package br.unitins.topicos1.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class SugestaoIaItem extends DefaultEntity {

    @ManyToOne
    @JoinColumn(name = "sugestao_id")
    private SugestaoIa sugestao;

    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "economia_reais", precision = 10, scale = 2)
    private BigDecimal economiaEstimadaReais;

    public SugestaoIa getSugestao() {
        return sugestao;
    }

    public void setSugestao(SugestaoIa sugestao) {
        this.sugestao = sugestao;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getEconomiaEstimadaReais() {
        return economiaEstimadaReais;
    }

    public void setEconomiaEstimadaReais(BigDecimal economiaEstimadaReais) {
        this.economiaEstimadaReais = economiaEstimadaReais;
    }
}


