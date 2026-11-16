package br.unisinos.ecommerce.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private static final Set<String> CAMPOS_ORDENACAO = Set.of("id", "nome", "preco", "estoque");
    private static final int TAMANHO_MAXIMO_PAGINA = 100;

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

    @Transactional
    public ProdutoResponseDTO salvar(ProdutoRequestDTO dto) {
        Categoria categoria = categoriaRepository.findById(dto.categoriaId())
            .orElseThrow(() -> new RegraNegocioException("Categoria não encontrada"));
        Produto produto = new Produto(dto.nome().trim(), normalizarDescricao(dto.descricao()),
            dto.preco(), dto.estoque(), categoria);
        return toResponseDTO(produtoRepository.save(produto));
    }

    public List<ProdutoResponseDTO> listarTodas() {
        return produtoRepository.findAll().stream()
            .map(this::toResponseDTO)
            .toList();
    }

    public Optional<ProdutoResponseDTO> buscarPorId(Long id) {
        return produtoRepository.findById(id).map(this::toResponseDTO);
    }

    @Transactional
    public ProdutoResponseDTO atualizar(Long id, ProdutoRequestDTO dto) {
        Produto produto = produtoRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado"));
        Categoria categoria = categoriaRepository.findById(dto.categoriaId())
            .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada"));
        produto.setNome(dto.nome().trim());
        produto.setDescricao(normalizarDescricao(dto.descricao()));
        produto.setPreco(dto.preco());
        produto.setEstoque(dto.estoque());
        produto.setCategoria(categoria);
        return toResponseDTO(produtoRepository.save(produto));
    }

    @Transactional
    public void excluir(Long id) {
        if (!produtoRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Produto não encontrado");
        }
        produtoRepository.deleteById(id);
    }

    // --- Consultas avançadas ---

    public Page<ProdutoResponseDTO> buscar(String nome, Long categoriaId, int page, int size, String ordenarPor) {
        Pageable pageable = PageRequest.of(validarPagina(page), validarTamanho(size), montarOrdenacao(ordenarPor));
        String filtroNome = (nome == null || nome.isBlank()) ? null : nome.trim();
        return produtoRepository.buscarComFiltros(filtroNome, categoriaId, pageable)
            .map(this::toResponseDTO);
    }

    public List<ProdutoPorCategoriaProjection> relatorioProdutosPorCategoria() {
        return produtoRepository.relatorioProdutosPorCategoria();
    }

    @Transactional
    public ProdutoResponseDTO atualizarEstoque(Long id, AtualizarEstoqueDTO dto) {
        Produto produto = produtoRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado"));
        produto.setEstoque(dto.estoque());
        return toResponseDTO(produtoRepository.save(produto));
    }

    // --- Métodos auxiliares ---

    private int validarPagina(int page) {
        if (page < 0) {
            throw new RegraNegocioException("O número da página não pode ser negativo");
        }
        return page;
    }

    private int validarTamanho(int size) {
        if (size < 1 || size > TAMANHO_MAXIMO_PAGINA) {
            throw new RegraNegocioException("O tamanho da página deve estar entre 1 e " + TAMANHO_MAXIMO_PAGINA);
        }
        return size;
    }

    private Sort montarOrdenacao(String ordenarPor) {
        if (ordenarPor == null || !CAMPOS_ORDENACAO.contains(ordenarPor)) {
            throw new RegraNegocioException(
                "Campo de ordenação inválido. Valores aceitos: id, nome, preco, estoque");
        }
        Sort.Order ordem = Sort.Order.asc(ordenarPor);
        if ("nome".equals(ordenarPor)) {
            ordem = ordem.ignoreCase(); // "iPhone" fica junto dos demais nomes com "I"
        }
        Sort ordenacao = Sort.by(ordem);
        // desempate pelo id garante uma paginação estável (sem itens repetidos entre páginas)
        return "id".equals(ordenarPor) ? ordenacao : ordenacao.and(Sort.by("id"));
    }

    private String normalizarDescricao(String descricao) {
        return (descricao == null || descricao.isBlank()) ? null : descricao.trim();
    }
}
