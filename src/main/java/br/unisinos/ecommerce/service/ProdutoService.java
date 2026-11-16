package br.unisinos.ecommerce.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import br.unisinos.ecommerce.dto.AtualizarEstoqueDTO;
import br.unisinos.ecommerce.dto.ProdutoRequestDTO;
import br.unisinos.ecommerce.dto.ProdutoResponseDTO;
import br.unisinos.ecommerce.entity.Categoria;
import br.unisinos.ecommerce.entity.Produto;
import br.unisinos.ecommerce.exception.RecursoNaoEncontradoException;
import br.unisinos.ecommerce.exception.RegraNegocioException;
import br.unisinos.ecommerce.repository.CategoriaRepository;
import br.unisinos.ecommerce.repository.ProdutoPorCategoriaProjection;
import br.unisinos.ecommerce.repository.ProdutoRepository;

@Service
public class ProdutoService {

    private final CategoriaRepository categoriaRepository;
    private final ProdutoRepository produtoRepository;

    public ProdutoService(CategoriaRepository categoriaRepository, ProdutoRepository produtoRepository) {
		super();
		this.categoriaRepository = categoriaRepository;
        this.produtoRepository = produtoRepository;
	}

    private ProdutoResponseDTO toResponseDTO(Produto p) {
        return new ProdutoResponseDTO(
            p.getId(), p.getNome(), p.getDescricao(), p.getPreco(), p.getEstoque(),
            p.getCategoria() != null ? p.getCategoria().getId() : null,
            p.getCategoria() != null ? p.getCategoria().getNome() : null
        );
    }
    
//    produtoDTO:
//    { "id" : "01", "descricao": "", "preco":"10", "estoque":2, "categoriaId": 1, "categoriaNome": "Eletronicos" } 
//    
//    produto
//    { "id" : "01", "descricao": "", "preco":"10", "estoque":2, "categoria": { "id": 1, "nome": "Eletronicos", "produtos": [] } } 
//    
//    categoria
//    { "nome": "Eletronicos", "produtos": [], "id": 1 }

    public List<ProdutoResponseDTO> listarTodas() {
        return produtoRepository.findAll().stream()
            .map(this::toResponseDTO)
            .toList();
    }

    public Optional<ProdutoResponseDTO> buscarPorId(Long id) {
        return produtoRepository.findById(id).map(this::toResponseDTO);
    }
    
    public void excluir(Long id) {
        produtoRepository.deleteById(id);
    }
    
    
    public ProdutoResponseDTO salvar(ProdutoRequestDTO dto) {
    	
//      produtoDTO:
//      { "id" : "01", "descricao": "", "preco":"10", "estoque":2, "categoriaId": 1} 
    	
        Categoria categoria = categoriaRepository.findById(dto.categoriaId())
            .orElseThrow(() -> new RegraNegocioException("Categoria não encontrada"));
        
        Produto produto = new Produto(dto.nome(), dto.descricao(), dto.preco(), dto.estoque(), categoria);
        
        return toResponseDTO(produtoRepository.save(produto));
    }

    public ProdutoResponseDTO atualizar(Long id, ProdutoRequestDTO dto) {	
        Produto produto = produtoRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado"));
        
        Categoria categoria = categoriaRepository.findById(dto.categoriaId())
            .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada"));
        
        produto.setNome(dto.nome());
        produto.setDescricao(dto.descricao());
        produto.setPreco(dto.preco());
        produto.setEstoque(dto.estoque());
        produto.setCategoria(categoria);
        
        return toResponseDTO(produtoRepository.save(produto));
    }
    
//    novo métodos
    
    public Page<ProdutoResponseDTO> buscar(String nome, Long categoriaId, int page, int size, String ordenarPor) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(ordenarPor));
        return produtoRepository.buscarComFiltros(nome, categoriaId, pageable)
            .map(this::toResponseDTO);
    }

    public List<ProdutoPorCategoriaProjection> relatorioProdutosPorCategoria() {
        return produtoRepository.relatorioProdutosPorCategoria();
    }

    public ProdutoResponseDTO atualizarEstoque(Long id, AtualizarEstoqueDTO dto) {
        Produto produto = produtoRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado"));
        produto.setEstoque(dto.estoque());
        return toResponseDTO(produtoRepository.save(produto));
    }
}