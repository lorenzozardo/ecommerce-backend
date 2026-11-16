package br.unisinos.ecommerce;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import br.unisinos.ecommerce.entity.Produto;
import br.unisinos.ecommerce.repository.ProdutoRepository;

@SpringBootApplication
public class EcommerceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcommerceApplication.class, args);
	}
	
//	@Bean
//    CommandLineRunner initDatabase(ProdutoRepository produtoRepository) {
//        return args -> {
//
//        	Produto produto = new Produto("Notebook", "computador", BigDecimal.valueOf(144.00), 10, null); 
//        	Produto produto1 = new Produto("Mouse", "computador", BigDecimal.valueOf(144.00), 10, null); 
//           
//            produtoRepository.save(produto);
//            produtoRepository.save(produto1);
//            
//            List<Produto> listaProdutos = produtoRepository.findAll();
//            
//            for(Produto p : listaProdutos) {
//            	System.out.println(p.getNome());
//            }
//        };
//    }
}