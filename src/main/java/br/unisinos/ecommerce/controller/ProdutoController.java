package br.unisinos.ecommerce.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.unisinos.ecommerce.dto.AtualizarEstoqueDTO;
import br.unisinos.ecommerce.dto.ProdutoRequestDTO;
import br.unisinos.ecommerce.dto.ProdutoResponseDTO;
import br.unisinos.ecommerce.exception.RecursoNaoEncontradoException;
import br.unisinos.ecommerce.repository.ProdutoPorCategoriaProjection;
import br.unisinos.ecommerce.service.ProdutoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/produtos")
@Tag(name = "Produtos", description = "Cadastro, busca paginada, relatório e estoque de produtos")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        super();
        this.produtoService = produtoService;
    }

    @PostMapping
    @Operation(summary = "Cria um produto")
    public ResponseEntity<ProdutoResponseDTO> salvar(@Valid @RequestBody ProdutoRequestDTO dto) {
        ProdutoResponseDTO produto = this.produtoService.salvar(dto);
        return new ResponseEntity<ProdutoResponseDTO>(produto, HttpStatus.ACCEPTED);
    }

    @GetMapping("/todos")
    @Operation(summary = "Lista todos os produtos (sem paginação)")
    public ResponseEntity<List<ProdutoResponseDTO>> listarTodos() {
        List<ProdutoResponseDTO> listaProdutos = this.produtoService.listarTodas();
        return new ResponseEntity<List<ProdutoResponseDTO>>(listaProdutos, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um produto pelo id")
    public ResponseEntity<ProdutoResponseDTO> buscarPorId(@PathVariable Long id) {
        ProdutoResponseDTO produto = produtoService.buscarPorId(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado"));
        return ResponseEntity.ok(produto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza todos os dados de um produto")
    public ResponseEntity<ProdutoResponseDTO> atualizar(@PathVariable Long id,
            @Valid @RequestBody ProdutoRequestDTO dto) {
        ProdutoResponseDTO produtoAtualizado = produtoService.atualizar(id, dto);
        return ResponseEntity.ok(produtoAtualizado);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um produto")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        produtoService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    // --- Consultas avançadas ---

    @GetMapping
    @Operation(summary = "Busca paginada de produtos com filtros opcionais por nome e categoria")
    public ResponseEntity<Page<ProdutoResponseDTO>> buscar(
            @Parameter(description = "Trecho do nome (sem diferenciar maiúsculas/minúsculas)")
            @RequestParam(required = false) String nome,
            @Parameter(description = "Id da categoria")
            @RequestParam(required = false) Long categoriaId,
            @Parameter(description = "Número da página (começa em 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Quantidade de itens por página (1 a 100)")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo de ordenação: id, nome, preco ou estoque")
            @RequestParam(defaultValue = "nome") String ordenarPor) {
        Page<ProdutoResponseDTO> pagina = produtoService.buscar(nome, categoriaId, page, size, ordenarPor);
        return ResponseEntity.ok(pagina);
    }

    @GetMapping("/relatorio/por-categoria")
    @Operation(summary = "Relatório com a quantidade de produtos por categoria")
    public ResponseEntity<List<ProdutoPorCategoriaProjection>> relatorioPorCategoria() {
        List<ProdutoPorCategoriaProjection> relatorio = produtoService.relatorioProdutosPorCategoria();
        return ResponseEntity.ok(relatorio);
    }

    @PatchMapping("/{id}/estoque")
    @Operation(summary = "Atualiza somente o estoque de um produto")
    public ResponseEntity<ProdutoResponseDTO> atualizarEstoque(@PathVariable Long id,
            @Valid @RequestBody AtualizarEstoqueDTO dto) {
        ProdutoResponseDTO produto = produtoService.atualizarEstoque(id, dto);
        return ResponseEntity.ok(produto);
    }
}
