package curso.springboot.repository;

import java.util.List;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import curso.springboot.model.Pessoa;

@Repository
@Transactional
public interface PessoaRepository extends JpaRepository<Pessoa, Long>{
	
	@Query("select p from Pessoa p where p.nome like %?1%")
	List<Pessoa> findPessoaByName(String nome);
	
	@Query("select p from Pessoa p where p.sexopessoa = ?1")
	List<Pessoa> findPessoaBySexo(String sexopessoa);
	
	@Query("select p from Pessoa p where p.nome like %?1% and p.sexopessoa = ?2")
	List<Pessoa> findPessoaByNameSexo(String nome, String sexopessoa);
	
	//@Query("select p from Pessoa p Where p.nome like %?1% and (p.sexo = ?2 or ?2 = '')")
	//List<Pessoa> findPessoaByTeste(String nome, String sexopessoa);
	
	default Page<Pessoa> findPessoaByNamePage(String nome, Pageable pageable) {
		
		Pessoa pessoa = new Pessoa();
		pessoa.setNome(nome);
		
		//Configura a pesquisa para consultar por Partes do Nome no BD, igual Like em SQL
		ExampleMatcher exampleMatcher = ExampleMatcher
				.matchingAny().withMatcher("nome", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());		
		//Une o Objeto com o valor e a configuração para consultar
		Example<Pessoa> example = Example.of(pessoa, exampleMatcher);

		//Executa no Banco
		Page<Pessoa> pessoas = findAll(example, pageable);
		return pessoas;
	}
	
default Page<Pessoa> findPessoaBySexoPage(String nome, String sexo,Pageable pageable) {
		
		Pessoa pessoa = new Pessoa();
		pessoa.setNome(nome);
		pessoa.setSexopessoa(sexo);
		
		//Configura a pesquisa para consultar por Partes do Nome no BD, igual Like em SQL
		ExampleMatcher exampleMatcher = ExampleMatcher
				.matchingAny()
				.withMatcher("nome", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())	
				.withMatcher("sexopessoa", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());
		
		//Une o Objeto com o valor e a configuração para consultar
		Example<Pessoa> example = Example.of(pessoa, exampleMatcher);

		//Executa no Banco
		Page<Pessoa> pessoas = findAll(example, pageable);
		return pessoas;
	}

}
