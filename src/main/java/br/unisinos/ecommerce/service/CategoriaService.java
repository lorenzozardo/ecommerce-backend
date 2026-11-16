package br.unisinos.ecommerce.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.unisinos.ecommerce.entity.Categoria;
import br.unisinos.ecommerce.exception.RecursoNaoEncontradoException;
import br.unisinos.ecommerce.exception.RegraNegocioException;
import br.unisinos.ecommerce.repository.CategoriaRepository;
import br.unisinos.ecommerce.repository.ProdutoRepository;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProdutoRepository produtoRepository;

    public CategoriaService(CategoriaRepository categoriaRepository, ProdutoRepository produtoRepository) {
        super();
        this.categoriaRepository = categoriaRepository;
        this.produtoRepository = produtoRepository;
    }

    @Transactional
    public Categoria salvar(Categoria categoria) {
        // cria uma nova instância para ignorar "id" ou "produtos" que venham no JSON
        Categoria novaCategoria = new Categoria(categoria.getNome().trim());
        return categoriaRepository.save(novaCategoria);
    }

    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll(Sort.by("nome"));
    }

    public Optional<Categoria> buscarPorId(Long id) {
        return categoriaRepository.findById(id);
    }

    @Transactional
    public Categoria atualizar(Long id, Categoria categoriaAtualizada) {
        Categoria categoria = categoriaRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada"));
        categoria.setNome(categoriaAtualizada.getNome().trim());
        return categoriaRepository.save(categoria);
    }

    @Transactional
    public void excluir(Long id) {
        if (!categoriaRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Categoria não encontrada");
        }
        if (produtoRepository.existsByCategoriaId(id)) {
            throw new RegraNegocioException("Não é possível excluir a categoria, pois existem produtos vinculados a ela");
        }
        categoriaRepository.deleteById(id);
    }
}
