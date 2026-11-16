package br.unisinos.ecommerce;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

/**
 * Testes de integração dos endpoints de categoria.
 * Cada teste roda em uma transação que é desfeita no final,
 * então os dados do import.sql ficam sempre iguais.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CategoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveListarCategoriasDoImportSql() throws Exception {
        mockMvc.perform(get("/categorias"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(3))
            .andExpect(jsonPath("$[*].nome", containsInAnyOrder("Informática", "Periféricos", "Smartphones")));
    }

    @Test
    void deveBuscarCategoriaPorId() throws Exception {
        mockMvc.perform(get("/categorias/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value("Informática"))
            .andExpect(jsonPath("$.produtos.length()").value(5));
    }

    @Test
    void deveRetornar404ComMensagemQuandoCategoriaNaoExiste() throws Exception {
        mockMvc.perform(get("/categorias/999"))
            .andExpect(status().isNotFound())
            .andExpect(content().string("Categoria não encontrada"));
    }

    @Test
    void deveCriarCategoria() throws Exception {
        mockMvc.perform(post("/categorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nome": "  Eletrônicos  "}
                    """))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.nome").value("Eletrônicos"));
    }

    @Test
    void deveIgnorarIdEnviadoAoCriarCategoria() throws Exception {
        mockMvc.perform(post("/categorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"id": 1, "nome": "Games"}
                    """))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.nome").value("Games"));

        // a categoria 1 continua intacta
        mockMvc.perform(get("/categorias/1"))
            .andExpect(jsonPath("$.nome").value("Informática"));
    }

    @Test
    void deveRetornar400ComMapaDeErrosAoCriarCategoriaSemNome() throws Exception {
        mockMvc.perform(post("/categorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.nome").value("O nome da categoria é obrigatório"));
    }

    @Test
    void deveAtualizarCategoria() throws Exception {
        mockMvc.perform(put("/categorias/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nome": "Eletrônicos e Informática"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.nome").value("Eletrônicos e Informática"));
    }

    @Test
    void deveRetornar404AoAtualizarCategoriaInexistente() throws Exception {
        mockMvc.perform(put("/categorias/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nome": "Qualquer"}
                    """))
            .andExpect(status().isNotFound())
            .andExpect(content().string("Categoria não encontrada"));
    }

    @Test
    void naoDeveExcluirCategoriaComProdutos() throws Exception {
        mockMvc.perform(delete("/categorias/1"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Não é possível excluir a categoria, pois existem produtos vinculados a ela"));
    }

    @Test
    void deveExcluirCategoriaSemProdutos() throws Exception {
        MvcResult resultado = mockMvc.perform(post("/categorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nome": "Temporária"}
                    """))
            .andExpect(status().isAccepted())
            .andReturn();
        Number id = JsonPath.read(resultado.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(delete("/categorias/" + id))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/categorias/" + id))
            .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar404AoExcluirCategoriaInexistente() throws Exception {
        mockMvc.perform(delete("/categorias/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void devePermitirCorsParaOFrontend() throws Exception {
        mockMvc.perform(get("/categorias").header("Origin", "http://localhost:5173"))
            .andExpect(status().isOk())
            .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }
}
