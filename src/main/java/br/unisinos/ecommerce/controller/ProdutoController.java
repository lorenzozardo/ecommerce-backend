package br.unisinos.ecommerce.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
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
import br.unisinos.ecommerce.repository.ProdutoPorCategoriaProjection;
import br.unisinos.ecommerce.service.ProdutoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {
	
	private final ProdutoService produtoService;

	public ProdutoController(ProdutoService produtoService) {
		super();
		this.produtoService = produtoService;
	}
	
	@GetMapping("/listar-todos")
	public ResponseEntity<List<ProdutoResponseDTO>> listarTodos() {
		List<ProdutoResponseDTO> listaProdutos = produtoService.listarTodas();
		return new ResponseEntity<List<ProdutoResponseDTO>>(listaProdutos, HttpStatus.OK);
	}
	
	@PostMapping
    public ResponseEntity<ProdutoResponseDTO> salvar(@Valid @RequestBody ProdutoRequestDTO dto) {
		ProdutoResponseDTO produto = this.produtoService.salvar(dto);
		return new ResponseEntity<ProdutoResponseDTO>(produto, HttpStatus.ACCEPTED);
	}
	
    @GetMapping("/{id}")
    public ResponseEntity<ProdutoResponseDTO> buscarPorId(@PathVariable Long id) {
        
        Optional<ProdutoResponseDTO> produto = produtoService.buscarPorId(id);

        if (produto.isPresent()) {
            return ResponseEntity.ok(produto.get());
        }

        return ResponseEntity.notFound().build();
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ProdutoResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody ProdutoRequestDTO dto) {
        ProdutoResponseDTO produtoAtualizado = produtoService.atualizar(id, dto);
        return ResponseEntity.ok(produtoAtualizado);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        
        Optional<ProdutoResponseDTO> produto = produtoService.buscarPorId(id);

        if (produto.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        produtoService.excluir(id);

        return ResponseEntity.noContent().build();
    }
    
//    novos métodos
    
    @GetMapping
    public ResponseEntity<Page<ProdutoResponseDTO>> buscar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "nome") String ordenarPor) {
        Page<ProdutoResponseDTO> pagina = produtoService.buscar(nome, categoriaId, page, size, ordenarPor);
        return ResponseEntity.ok(pagina);
    }

    @GetMapping("/relatorio/por-categoria")
    public ResponseEntity<List<ProdutoPorCategoriaProjection>> relatorioPorCategoria() {
        List<ProdutoPorCategoriaProjection> relatorio = produtoService.relatorioProdutosPorCategoria();
        return ResponseEntity.ok(relatorio);
    }

    @PatchMapping("/{id}/estoque")
    public ResponseEntity<ProdutoResponseDTO> atualizarEstoque(@PathVariable Long id,
            @Valid @RequestBody AtualizarEstoqueDTO dto) {
        ProdutoResponseDTO produto = produtoService.atualizarEstoque(id, dto);
        return ResponseEntity.ok(produto);
    }
    
    @PatchMapping("/{id}/estoque/{qty}")
    public ResponseEntity<ProdutoResponseDTO> atualizarEstoqueII(@PathVariable Long id,
    		@PathVariable int qty) {
    	AtualizarEstoqueDTO atualizarEstoqueDTO = new AtualizarEstoqueDTO(qty);
        ProdutoResponseDTO produto = produtoService.atualizarEstoque(id, atualizarEstoqueDTO);
        return ResponseEntity.ok(produto);
    }
    
	
}
