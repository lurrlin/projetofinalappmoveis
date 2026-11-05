package com.example.projetofinal.localizacao;

import java.util.ArrayList;
import java.util.List;

public final class FormatadorEndereco {
    private FormatadorEndereco() { }

    public static String formatar(String bairro, String cidade, String estado) {
        bairro = limpar(bairro);
        cidade = limpar(cidade);
        estado = limpar(estado);
        List<String> partes = new ArrayList<>();
        if (!bairro.isEmpty() && !bairro.equalsIgnoreCase(cidade)) partes.add(bairro);
        if (!cidade.isEmpty()) partes.add(cidade);
        if (!estado.isEmpty()) partes.add(estado);
        if (partes.isEmpty()) return null;
        StringBuilder endereco = new StringBuilder();
        for (String parte : partes) {
            if (endereco.length() > 0) endereco.append(" · ");
            endereco.append(parte);
        }
        return endereco.toString();
    }

    private static String limpar(String texto) {
        return texto == null ? "" : texto.trim();
    }
}
