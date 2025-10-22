package br.unitins.topicos1.model;

public enum TipoUso {
    DOMESTICO(1, "Uso Doméstico"),
    EMPRESARIAL(2, "Uso Empresarial"),
    INDUSTRIAL(3, "Uso Industrial"),
    AGRICOLA(4, "Uso Agrícola"),
    COMERCIAL(5, "Uso Comercial");

    private final Integer id;
    private final String descricao;

    TipoUso(Integer id, String descricao) {
        this.id = id;
        this.descricao = descricao;
    }

    public Integer getId() {
        return id;
    }

    public String getDescricao() {
        return descricao;
    }

    public static TipoUso valueOf(Integer id) {
        if (id == null)
            return null;
        for (TipoUso tipo : TipoUso.values()) {
            if (tipo.getId().equals(id))
                return tipo;
        }
        return null;
    }
}
