package br.unitins.topicos1.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Leitura extends DefaultEntity {
    @ManyToOne
    @JoinColumn(name = "medidor_id")
    private Medidor medidor;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal vazao;
    
    @Column(name = "consumo_total", precision = 10, scale = 2)
    private BigDecimal consumoTotal;
    
    @Column(name = "data_hora")
    private LocalDateTime dataHora;

    public Medidor getMedidor() {
        return medidor;
    }

    public void setMedidor(Medidor medidor) {
        this.medidor = medidor;
    }

    public BigDecimal getVazao() {
        return vazao;
    }

    public void setVazao(BigDecimal vazao) {
        this.vazao = vazao;
    }

    public BigDecimal getConsumoTotal() {
        return consumoTotal;
    }

    public void setConsumoTotal(BigDecimal consumoTotal) {
        this.consumoTotal = consumoTotal;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }
}
