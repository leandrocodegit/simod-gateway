package br.sincroled.gateway.models;

public enum DiasSemana {
    SEG(1),
    TER(2),
    QUA(3),
    QUI(4),
    SEX(5),
    SAB(6),
    DOM(7);

    public int dia;

    DiasSemana(int dia) {
        this.dia = dia;
    }
}
