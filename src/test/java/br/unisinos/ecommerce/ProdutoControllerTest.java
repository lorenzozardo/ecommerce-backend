package br.unisinos.ecommerce;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Testes de integração dos endpoints de produto.
 * O banco começa com os 13 produtos do import.sql (5 + 5 + 3 por categoria).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProdutoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // ---------- CRUD ----------

    @Test
    void deveCriarProduto() throws Exception {
        mockMvc.perform(post("/produtos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nome": "Notebook Dell", "descricao": "Notebook i7 16GB RAM",
                     "preco": 4999.99, "estoque": 10, "categoriaId": 1}
                    """))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.nome").value("Notebook Dell"))
            .andExpect(jsonPath("$.preco").value(4999.99))
            .andExpect(jsonPath("$.categoriaId").value(1))
            .andExpect(jsonPath("$.nomeCategoria").value("Informática"));
    }

    @Test
    void deveAceitarDescricaoCom500Caracteres() throws Exception {
        String descricao = "a".repeat(500);
        mockMvc.perform(post("/produtos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nome": "Produto descrito", "descricao": "%s",
                     "preco": 10, "estoque": 1, "categoriaId": 2}
                    """.formatted(descricao)))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.descricao").value(descricao));
    }

    @Test
    void deveListarTodosOsProdutos() throws Exception {
        mockMvc.perform(get("/produtos/todos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(13));
    }

    @Test
    void deveBuscarProdutoPorId() throws Exception {
        mockMvc.perform(get("/produtos/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value("Notebook Dell i7"))
            .andExpect(jsonPath("$.nomeCategoria").value("Informática"));
    }

    @Test
    void deveRetornar404QuandoProdutoNaoExiste() throws Exception {
        mockMvc.perform(get("/produtos/999"))
            .andExpect(status().isNotFound())
            .andExpect(content().string("Produto não encontrado"));
    }

    @Test
    void deveAtualizarProduto() throws Exception {
        mockMvc.perform(put("/produtos/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nome": "Notebook Dell XPS", "descricao": "Notebook i9 32GB RAM",
                     "preco": 7999.99, "estoque": 5, "categoriaId": 1}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value("Notebook Dell XPS"))
            .andExpect(jsonPath("$.estoque").value(5));
    }

    @Test
    void deveExcluirProduto() throws Exception {
        mockMvc.perform(delete("/produtos/1"))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/produtos/1"))
            .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar404AoExcluirProdutoInexistente() throws Exception {
        mockMvc.perform(delete("/produtos/999"))
            .andExpect(status().isNotFound());
    }

    // ---------- Exceções e validação ----------

    @Test
    void deveRetornar400QuandoCategoriaDoProdutoNaoExiste() throws Exception {
        mockMvc.perform(post("/produtos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nome": "Produto", "preco": 10, "estoque": 1, "categoriaId": 999}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Categoria não encontrada"));
    }

    @Test
    void deveRetornar400ComMapaDeErrosQuandoProdutoInvalido() throws Exception {
        mockMvc.perform(post("/produtos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nome": "", "preco": 0, "estoque": -1}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.nome").value("O nome do produto é obrigatório"))
            .andExpect(jsonPath("$.preco").value("O preço deve ser maior que zero"))
            .andExpect(jsonPath("$.estoque").value("O estoque não pode ser negativo"))
            .andExpect(jsonPath("$.categoriaId").value("A categoria é obrigatória"));
    }

    @Test
    void deveResponderMensagemDeErroComoTextoMesmoQuandoClientePedeJson() throws Exception {
        // mesmo Accept enviado pelo axios no frontend
        mockMvc.perform(get("/produtos/999").accept(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN, MediaType.ALL))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType("text/plain;charset=UTF-8"))
            .andExpect(content().string("Produto não encontrado"));
    }

    @Test
    void deveRetornar400QuandoJsonMalFormatado() throws Exception {
        mockMvc.perform(post("/produtos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\": \"Produto\", \"preco\": \"abc\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Corpo da requisição inválido ou mal formatado"));
    }

    @Test
    void deveRetornar400QuandoIdNaoNumerico() throws Exception {
        mockMvc.perform(get("/produtos/abc"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Valor inválido para o parâmetro 'id'"));
    }

    // ---------- Busca paginada, relatório e PATCH ----------

    @Test
    void deveBuscarPrimeiraPaginaSemFiltros() throws Exception {
        mockMvc.perform(get("/produtos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(10))
            .andExpect(jsonPath("$.totalElements").value(13))
            .andExpect(jsonPath("$.totalPages").value(2))
            .andExpect(jsonPath("$.number").value(0))
            .andExpect(jsonPath("$.size").value(10))
            .andExpect(jsonPath("$.first").value(true))
            .andExpect(jsonPath("$.last").value(false))
            // ordenado por nome sem diferenciar maiúsculas (iPhone entre Headset e Memória)
            .andExpect(jsonPath("$.content[0].nome").value("Headset HyperX"))
            .andExpect(jsonPath("$.content[1].nome").value("iPhone 15"));
    }

    @Test
    void deveBuscarSegundaPagina() throws Exception {
        mockMvc.perform(get("/produtos").param("page", "1").param("size", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(5))
            .andExpect(jsonPath("$.number").value(1))
            .andExpect(jsonPath("$.totalPages").value(3))
            .andExpect(jsonPath("$.first").value(false))
            .andExpect(jsonPath("$.last").value(false));
    }

    @Test
    void deveFiltrarPorNomeSemDiferenciarMaiusculas() throws Exception {
        mockMvc.perform(get("/produtos").param("nome", "NOTEBOOK"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(3))
            .andExpect(jsonPath("$.content[*].nome",
                containsInAnyOrder("Notebook Dell i7", "Notebook Lenovo i5", "Notebook Apple M3")));
    }

    @Test
    void deveFiltrarPorCategoria() throws Exception {
        mockMvc.perform(get("/produtos").param("categoriaId", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(5))
            .andExpect(jsonPath("$.content[*].nomeCategoria", everyItem(org.hamcrest.Matchers.is("Periféricos"))));
    }

    @Test
    void deveFiltrarPorNomeECategoria() throws Exception {
        mockMvc.perform(get("/produtos").param("nome", "logitech").param("categoriaId", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void deveOrdenarPorPreco() throws Exception {
        mockMvc.perform(get("/produtos").param("ordenarPor", "preco").param("size", "3"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].nome").value("Memória RAM 16GB"))
            .andExpect(jsonPath("$.content[1].nome").value("Mouse Logitech MX"))
            .andExpect(jsonPath("$.content[2].nome").value("Teclado Mecânico Redragon"));
    }

    @Test
    void deveRetornar400ParaCampoDeOrdenacaoInvalido() throws Exception {
        mockMvc.perform(get("/produtos").param("ordenarPor", "senha"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar400ParaTamanhoDePaginaInvalido() throws Exception {
        mockMvc.perform(get("/produtos").param("size", "0"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void deveGerarRelatorioPorCategoria() throws Exception {
        mockMvc.perform(get("/produtos/relatorio/por-categoria"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(3))
            .andExpect(jsonPath("$[0].categoria").value("Informática"))
            .andExpect(jsonPath("$[0].quantidade").value(5))
            .andExpect(jsonPath("$[1].categoria").value("Periféricos"))
            .andExpect(jsonPath("$[1].quantidade").value(5))
            .andExpect(jsonPath("$[2].categoria").value("Smartphones"))
            .andExpect(jsonPath("$[2].quantidade").value(3));
    }

    @Test
    void deveAtualizarEstoqueComPatch() throws Exception {
        mockMvc.perform(patch("/produtos/1/estoque")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"estoque\": 99}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estoque").value(99))
            .andExpect(jsonPath("$.nome").value("Notebook Dell i7"));
    }

    @Test
    void deveRetornar400AoAtualizarEstoqueNegativo() throws Exception {
        mockMvc.perform(patch("/produtos/1/estoque")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"estoque\": -5}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.estoque").value("O estoque não pode ser negativo"));
    }

    @Test
    void deveRetornar404AoAtualizarEstoqueDeProdutoInexistente() throws Exception {
        mockMvc.perform(patch("/produtos/999/estoque")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"estoque\": 1}"))
            .andExpect(status().isNotFound());
    }
}
