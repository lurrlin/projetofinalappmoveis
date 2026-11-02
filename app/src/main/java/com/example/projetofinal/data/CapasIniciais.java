package com.example.projetofinal.data;

/** Identificadores conferidos pela Search API da Open Library. Algumas edições são em inglês. */
public final class CapasIniciais {
    private CapasIniciais() { }
    public static String obterUrl(String identificador) {
        int capa;
        switch (identificador) {
            case "harry-potter-1": capa = 15155833; break;
            case "o-hobbit": capa = 15121777; break;
            case "sherlock": capa = 6717853; break;
            case "dom-casmurro": capa = 647501; break;
            case "o-iluminado": capa = 12376585; break;
            case "dracula": capa = 12216503; break;
            default: return "";
        }
        return "https://covers.openlibrary.org/b/id/" + capa + "-M.jpg?default=false";
    }
}
