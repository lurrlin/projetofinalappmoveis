package com.example.projetofinal.localizacao;

import org.junit.Test;
import static org.junit.Assert.*;

public class FormatadorEnderecoTest {
    @Test public void mostraBairroCidadeEstadoSemEspacosExtras() {
        assertEquals("Centro · Florianópolis · Santa Catarina",
                FormatadorEndereco.formatar(" Centro ", "Florianópolis", " Santa Catarina "));
    }

    @Test public void usaSomenteOsCamposDisponiveis() {
        assertEquals("Florianópolis · Santa Catarina",
                FormatadorEndereco.formatar(null, "Florianópolis", "Santa Catarina"));
        assertEquals("Santa Catarina", FormatadorEndereco.formatar(" ", null, "Santa Catarina"));
        assertNull(FormatadorEndereco.formatar(null, " ", null));
    }

    @Test public void naoRepeteBairroQuandoServicoRetornaNomeDaCidade() {
        assertEquals("Florianópolis · Santa Catarina",
                FormatadorEndereco.formatar("florianópolis", "Florianópolis", "Santa Catarina"));
    }

    @Test public void preservaCidadeEEstadoComMesmoNome() {
        assertEquals("Sé · São Paulo · São Paulo", FormatadorEndereco.formatar("Sé", "São Paulo", "São Paulo"));
    }
}
