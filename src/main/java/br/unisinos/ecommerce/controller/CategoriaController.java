package br.unisinos.ecommerce.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.unisinos.ecommerce.entity.Categoria;
import br.unisinos.ecommerce.service.CategoriaService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
		super();
		this.categoriaService = categoriaService;
	}

	@PostMapping
    public ResponseEntity<Categoria> salvar(@Valid @RequestBody Categoria categoria) {
		this.categoriaService.salvar(categoria);
		return new ResponseEntity<Categoria>(categoria, HttpStatus.ACCEPTED);
	}
	
//	localhost:8080/categorias
    @GetMapping
    public ResponseEntity<List<Categoria>> listarTodos() {
    	List<Categoria> listaCategoria = this.categoriaService.listarTodas();
    	return new ResponseEntity<List<Categoria>>(listaCategoria, HttpStatus.OK);
    }
    
//    localhost:8080/categorias/002
    @GetMapping("/{id}")
    public ResponseEntity<Categoria> buscarPorId(@PathVariable Long id) {
        
        Optional<Categoria> categoria = categoriaService.buscarPorId(id);

        if (categoria.isPresent()) {
            return ResponseEntity.ok(categoria.get());
        }

        return ResponseEntity.notFound().build();
    }
    
//  PUT localhost:8080/categorias/001
    @PutMapping("/{id}")
    public ResponseEntity<Categoria> atualizar(@PathVariable Long id, @Valid @RequestBody Categoria categoria) {
        Categoria categoriaAtualizado = categoriaService.atualizar(id, categoria);
        return ResponseEntity.ok(categoriaAtualizado);
    }
  
//  DELETE localhost:8080/categorias/001
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        
        Optional<Categoria> categoria = categoriaService.buscarPorId(id);

        if (categoria.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        categoriaService.excluir(id);

        return ResponseEntity.noContent().build();
    }
}