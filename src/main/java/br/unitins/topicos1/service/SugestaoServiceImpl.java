package br.unitins.topicos1.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import br.unitins.topicos1.dto.EstatisticaResponseDTO;
import br.unitins.topicos1.dto.SugestaoDTO;
import br.unitins.topicos1.dto.SugestaoResponseDTO;
import br.unitins.topicos1.model.Medidor;
import br.unitins.topicos1.model.Sugestao;
import br.unitins.topicos1.model.TipoUso;
import br.unitins.topicos1.model.Usuario;
import br.unitins.topicos1.repository.MedidorRepository;
import br.unitins.topicos1.repository.SugestaoRepository;
import br.unitins.topicos1.repository.UsuarioRepository;
import br.unitins.topicos1.validation.ValidationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@ApplicationScoped
public class SugestaoServiceImpl implements SugestaoService {

    @Inject
    public SugestaoRepository sugestaoRepository;

    @Inject
    public UsuarioRepository usuarioRepository;

    @Inject
    public MedidorRepository medidorRepository;

    @Inject
    public EstatisticaService estatisticaService;

    @Override
    @Transactional
    public SugestaoResponseDTO create(@Valid SugestaoDTO dto) {
        Usuario usuario = usuarioRepository.findById(dto.usuarioId());
        if (usuario == null)
            throw new ValidationException("usuarioId", "Usuário não encontrado.");

        Sugestao sugestao = new Sugestao();
        sugestao.setUsuario(usuario);
        sugestao.setMensagem(dto.mensagem());
        sugestao.setDataHora(LocalDateTime.now());

        sugestaoRepository.persist(sugestao);
        return SugestaoResponseDTO.valueOf(sugestao);
    }

    @Override
    public List<SugestaoResponseDTO> findByUsuarioId(Long usuarioId) {
        return sugestaoRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(SugestaoResponseDTO::valueOf)
                .toList();
    }

    @Override
    @Transactional
    public List<SugestaoResponseDTO> gerarSugestoesIA(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId);
        if (usuario == null)
            throw new ValidationException("usuarioId", "Usuário não encontrado.");

        List<Medidor> medidores = medidorRepository.findByUsuarioId(usuarioId);
        List<SugestaoResponseDTO> sugestoes = new ArrayList<>();

        for (Medidor medidor : medidores) {
            EstatisticaResponseDTO stats = estatisticaService.calcularEstatisticas(medidor.getId());
            
            // Gerar sugestões baseadas em estatísticas, tipo de uso e localização
            List<String> mensagens = analisarEGerarSugestoes(
                stats, 
                usuario.getTipoUso(), 
                medidor.getLocalizacao()
            );
            
            for (String mensagem : mensagens) {
                Sugestao sugestao = new Sugestao();
                sugestao.setUsuario(usuario);
                sugestao.setMensagem(mensagem);
                sugestao.setDataHora(LocalDateTime.now());
                
                sugestaoRepository.persist(sugestao);
                sugestoes.add(SugestaoResponseDTO.valueOf(sugestao));
            }
        }

        return sugestoes;
    }

    private List<String> analisarEGerarSugestoes(
            EstatisticaResponseDTO stats, 
            TipoUso tipoUso, 
            String tipoLocalizacao) {
        
        List<String> sugestoes = new ArrayList<>();

        // Sugestões baseadas no TIPO DE USO
        if (tipoUso == TipoUso.DOMESTICO) {
            sugestoes.addAll(gerarSugestoesDomesticas(stats, tipoLocalizacao));
        } else if (tipoUso == TipoUso.INDUSTRIAL) {
            sugestoes.addAll(gerarSugestoesIndustriais(stats, tipoLocalizacao));
        } else if (tipoUso == TipoUso.AGRICOLA) {
            sugestoes.addAll(gerarSugestoesAgricolas(stats, tipoLocalizacao));
        }

        // Análises gerais independente do tipo
        if (stats.vazaoMedia().doubleValue() > 20) {
            sugestoes.add("⚠️ Vazão média muito alta (" + stats.vazaoMedia() + " L/min) detectada no + Verifique possíveis vazamentos imediatamente!");
        }

        return sugestoes;
    }

    private List<String> gerarSugestoesDomesticas(EstatisticaResponseDTO stats, String tipo) {
        List<String> sugestoes = new ArrayList<>();

        switch (tipo) {
            case "CHUVEIRO":
                if (stats.consumoDiario().doubleValue() > 200) {
                    sugestoes.add("🚿 Seu chuveiro está consumindo " + stats.consumoDiario() + "L/dia. " +
                                 "Reduza o tempo de banho em 3 minutos e economize até 45L/dia. " +
                                 "Considere instalar um redutor de vazão (economia de 30%).");
                }
                break;

            case "TORNEIRA":
                if (stats.consumoDiario().doubleValue() > 150) {
                    sugestoes.add("🚰 Alto consumo na torneira: " + stats.consumoDiario() + "L/dia. " +
                                 "Instale arejadores (economia de 50%) e feche a torneira ao ensaboar as mãos.");
                }
                break;

            case "VASO_SANITARIO":
                if (stats.consumoDiario().doubleValue() > 100) {
                    sugestoes.add("🚽 Consumo elevado no vaso sanitário: " + stats.consumoDiario() + "L/dia. " +
                                 "Considere trocar por modelo de descarga dupla (economia de 6L por uso).");
                }
                break;

            case "MANGUEIRA":
                if (stats.consumoDiario().doubleValue() > 300) {
                    sugestoes.add("💧 Mangueira consumindo " + stats.consumoDiario() + "L/dia. " +
                                 "Use balde para lavar o carro (economia de 300L) e regue plantas nas horas mais frescas.");
                }
                break;

            case "MAQUINA_LAVAR":
                if (stats.consumoSemanal().doubleValue() > 500) {
                    sugestoes.add("👕 Máquina de lavar: " + stats.consumoSemanal() + "L/semana. " +
                                 "Lave roupas apenas com carga completa e reutilize a água para limpeza.");
                }
                break;
        }

        // Sugestão positiva
        if (stats.consumoDiario().doubleValue() < 80) {
            sugestoes.add("✅ Parabéns! Seu consumo doméstico está excelente: " + 
                         stats.consumoDiario() + "L/dia. Continue com esses hábitos conscientes!");
        }

        return sugestoes;
    }

    private List<String> gerarSugestoesEmpresariais(EstatisticaResponseDTO stats, String tipo) {
        List<String> sugestoes = new ArrayList<>();

        if (stats.consumoMensal().doubleValue() > 50000) {
            sugestoes.add("🏢 Consumo empresarial elevado: " + stats.consumoMensal() + "L/mês. " +
                         "Recomendamos auditoria hidráulica profissional e instalação de sistema de reúso.");
        }

        switch (tipo) {
            case "TORNEIRA":
                sugestoes.add("💼 Torneiras empresariais: Instale torneiras com sensor automático " +
                             "(economia de até 70%) e implemente programa de conscientização de funcionários.");
                break;

            case "VASO_SANITARIO":
                sugestoes.add("🏢 Banheiros corporativos: Mictórios sem água podem economizar 150.000L/ano. " +
                             "Considere também sistema de captação de água da chuva.");
                break;

            case "BEBEDOURO":
                if (stats.consumoDiario().doubleValue() > 200) {
                    sugestoes.add("💧 Bebedouro com alto consumo. Verifique se há vazamentos internos " +
                                 "e considere modelo com refrigeração eficiente.");
                }
                break;
        }

        return sugestoes;
    }

    private List<String> gerarSugestoesIndustriais(EstatisticaResponseDTO stats, String tipo) {
        List<String> sugestoes = new ArrayList<>();

        if (stats.consumoMensal().doubleValue() > 500000) {
            sugestoes.add("🏭 Alto consumo industrial: " + stats.consumoMensal() + "L/mês. " +
                         "Implemente sistema de recirculação e tratamento de efluentes para reúso (economia de 40-60%).");
        }

        sugestoes.add("⚙️ Indústria: Realize manutenção preventiva mensal em todo sistema hidráulico. " +
                     "Vazamentos industriais podem representar perda de 100.000L/dia.");

        if (stats.vazaoMaxima().doubleValue() > 50) {
            sugestoes.add("📊 Pico de vazão detectado: " + stats.vazaoMaxima() + " L/min. " +
                         "Otimize processos para distribuir consumo ao longo do dia e reduzir custos de bombeamento.");
        }

        return sugestoes;
    }

    private List<String> gerarSugestoesAgricolas(EstatisticaResponseDTO stats, String tipo) {
        List<String> sugestoes = new ArrayList<>();

        if (tipo == "IRRIGACAO") {
            if (stats.consumoDiario().doubleValue() > 5000) {
                sugestoes.add("🌾 Irrigação: " + stats.consumoDiario() + "L/dia. " +
                             "Implemente irrigação por gotejamento (economia de 50%) e irrigue nas primeiras " +
                             "horas da manhã ou final da tarde para evitar evaporação.");
            }

            sugestoes.add("🌱 Agricultura: Considere sistema de monitoramento de umidade do solo para " +
                         "irrigar apenas quando necessário. Economia potencial: 30-40%.");
        }

        if (stats.consumoMensal().doubleValue() > 100000) {
            sugestoes.add("💧 Consumo agrícola elevado. Técnicas de mulching (cobertura morta) podem " +
                         "reduzir evaporação em até 70% e diminuir necessidade de irrigação.");
        }

        return sugestoes;
    }
}

