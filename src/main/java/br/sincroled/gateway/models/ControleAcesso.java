package br.sincroled.gateway.models;

import java.util.List;


public class ControleAcesso {

    private Boolean ativo;
    private Boolean controlarDias;
    private Boolean controlarHorario;
    private List<DiasSemana> dias;
    private String inicio;
    private String fim;
    private Long tolerancia;

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public Boolean getControlarDias() {
        return controlarDias;
    }

    public void setControlarDias(Boolean controlarDias) {
        this.controlarDias = controlarDias;
    }

    public Boolean getControlarHorario() {
        return controlarHorario;
    }

    public void setControlarHorario(Boolean controlarHorario) {
        this.controlarHorario = controlarHorario;
    }

    public List<DiasSemana> getDias() {
        return dias;
    }

    public void setDias(List<DiasSemana> dias) {
        this.dias = dias;
    }

    public String getInicio() {
        return inicio;
    }

    public void setInicio(String inicio) {
        this.inicio = inicio;
    }

    public String getFim() {
        return fim;
    }

    public void setFim(String fim) {
        this.fim = fim;
    }

    public Long getTolerancia() {
        return tolerancia;
    }

    public void setTolerancia(Long tolerancia) {
        this.tolerancia = tolerancia;
    }
}
