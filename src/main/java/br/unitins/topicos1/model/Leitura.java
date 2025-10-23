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
    
    @Column(precision = 10, scale = 3, nullable = false)
    private BigDecimal litros;
    
    @Column(name = "litros_acumulado", precision = 12, scale = 2, nullable = false)
    private BigDecimal litrosAcumulado; 
    
    @Column(name = "vazao_l_min", precision = 10, scale = 3, nullable = true)
    private BigDecimal vazaoLMin;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    public Medidor getMedidor() {
        return medidor;
    }

    public void setMedidor(Medidor medidor) {
        this.medidor = medidor;
    }

    public BigDecimal getLitros() {
        return litros;
    }

    public void setLitros(BigDecimal litros) {
        this.litros = litros;
    }

    public BigDecimal getLitrosAcumulado() {
        return litrosAcumulado;
    }

    public void setLitrosAcumulado(BigDecimal litrosAcumulado) {
        this.litrosAcumulado = litrosAcumulado;
    }

    public BigDecimal getVazaoLMin() {
        return vazaoLMin;
    }

    public void setVazaoLMin(BigDecimal vazaoLMin) {
        this.vazaoLMin = vazaoLMin;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }
}
