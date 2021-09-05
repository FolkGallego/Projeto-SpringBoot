package curso.springboot.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import curso.springboot.model.Pessoa;
import curso.springboot.model.Telefone;
import curso.springboot.repository.PessoaRepository;
import curso.springboot.repository.ProfissaoRepository;
import curso.springboot.repository.TelefoneRepository;

@Controller
public class PessoaController {

	@Autowired
	private PessoaRepository pessoaRepository;

	@Autowired
	private TelefoneRepository telefoneRepository;

	@Autowired
	private ReportUtil reportUtil;
	
	@Autowired
	private ProfissaoRepository profissaoRepository;
	
	@RequestMapping(method = RequestMethod.GET, value = "**/cadastropessoa")
	public ModelAndView inicio() {
		
		ModelAndView modelAndView = new ModelAndView("cadastro/cadastropessoa");
		modelAndView.addObject("pessoaobj", new Pessoa());
		modelAndView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 3, Sort.by("nome"))));
		modelAndView.addObject("profissoes", profissaoRepository.findAll());
		return modelAndView;
	}
	
	@GetMapping("/pessoaspag")
	public ModelAndView carregaPessoaPorPaginacao(@PageableDefault(size = 3)Pageable pageable, ModelAndView model
			,@RequestParam("nomepesquisa") String nomepesquisa) {
		
		Page<Pessoa> pagePessoa = pessoaRepository.findPessoaByNamePage(nomepesquisa, pageable);
		model.addObject("pessoas", pagePessoa);
		model.addObject("pessoaobj", new Pessoa());
		model.addObject("nomepesquisa", nomepesquisa);
		model.setViewName("cadastro/cadastropessoa");
		
		return model;
	}

	@RequestMapping(method = RequestMethod.POST, value = "**/salvarpessoa", consumes = {"multipart/form-data"})
	public ModelAndView salvar(@Valid Pessoa pessoa, BindingResult bindingResult, final MultipartFile file) throws IOException {

		System.out.println(file.getContentType());
		System.out.println(file.getName());
		System.out.println(file.getOriginalFilename());
		
		// Precisa carregar o Objeto, pois o método é em cascate
		pessoa.setTelefones(telefoneRepository.getTelefones(pessoa.getId()));

		if (bindingResult.hasErrors()) {

			ModelAndView modelAndView = new ModelAndView("cadastro/cadastropessoa");
			modelAndView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 3, Sort.by("nome"))));
			modelAndView.addObject("pessoaobj", pessoa);

			List<String> msg = new ArrayList<String>();
			for (ObjectError objectError : bindingResult.getAllErrors()) {
				msg.add(objectError.getDefaultMessage());
			}

			modelAndView.addObject("msg", msg);
			modelAndView.addObject("profissoes", profissaoRepository.findAll());
			return modelAndView;
		}

		if (file.getSize() > 0) {//Cadastrando com Curriculo
			pessoa.setCurriculo(file.getBytes());
			pessoa.setTipoFileCurriculo(file.getContentType());
			pessoa.setNomeFileCurriculo(file.getOriginalFilename());
		}else {
			if(pessoa.getId() != null && pessoa.getId() > 0) {//Editando por isso ID > 0
				Pessoa pessoaTemp = pessoaRepository.findById(pessoa.getId()).get();//Carrega do Banco
				
				//Essas Linhas mantém respectivamente Arquivo, Tipo e Nome
				pessoa.setCurriculo(pessoaTemp.getCurriculo()); 
				pessoa.setTipoFileCurriculo(pessoaTemp.getTipoFileCurriculo());
				pessoa.setNomeFileCurriculo(pessoaTemp.getNomeFileCurriculo());
			}
		}
		
		pessoaRepository.save(pessoa);

		ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
		andView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 3, Sort.by("nome"))));
		andView.addObject("pessoaobj", new Pessoa());

		return andView;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/listapessoas")
	public ModelAndView pessoas() {
		ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
		andView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 3, Sort.by("nome"))));
		andView.addObject("pessoaobj", new Pessoa());

		return andView;
	}

	@GetMapping("/editarpessoa/{idpessoa}")
	public ModelAndView editar(@PathVariable("idpessoa") Long idpessoa) {
		Optional<Pessoa> pessoa = pessoaRepository.findById(idpessoa);
		ModelAndView modelAndView = new ModelAndView("cadastro/cadastropessoa");
		modelAndView.addObject("pessoaobj", pessoa.get());
		
		modelAndView.addObject("profissoes", profissaoRepository.findAll()); 
		return modelAndView;
	}

	@GetMapping("/removerpessoa/{idpessoa}")
	public ModelAndView excluir(@PathVariable("idpessoa") Long idpessoa) {

		pessoaRepository.deleteById(idpessoa);

		ModelAndView modelAndView = new ModelAndView("cadastro/cadastropessoa");
		modelAndView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 3, Sort.by("nome"))));
		modelAndView.addObject("pessoaobj", new Pessoa());

		return modelAndView;
	}

	@GetMapping("**/baixarcurriculo/{idpessoa}")
	public void baixarcurriculo(@PathVariable("idpessoa") Long idpessoa, 
			HttpServletResponse response) throws IOException {
		
		/*Consultar o obejto pessoa no banco de dados*/
		Pessoa pessoa = pessoaRepository.findById(idpessoa).get();
		if (pessoa.getCurriculo() != null) {
	
			/*Setar tamanho da resposta*/
			response.setContentLength(pessoa.getCurriculo().length);
			
			/*Tipo do arquivo para download ou pode ser generica application/octet-stream*/
			response.setContentType(pessoa.getTipoFileCurriculo());
			
			/*Define o cabe�alho da resposta*/
			String headerKey = "Content-Disposition";
			String headerValue = String.format("attachment; filename=\"%s\"", pessoa.getNomeFileCurriculo());
			response.setHeader(headerKey, headerValue);
			
			/*Finaliza a resposta passando o arquivo*/
			response.getOutputStream().write(pessoa.getCurriculo());
			
		}
		
	} 
	
	@PostMapping("**/pesquisarpessoa")
	public ModelAndView pesquisar(@RequestParam("nomepesquisa") String nomepesquisa,
			@RequestParam("pesqsexo") String pesqsexo, @PageableDefault(size = 3, sort = {"nome"}) Pageable pageable) {

		 Page<Pessoa> pessoas = null;

		if (pesqsexo != null && !pesqsexo.isEmpty()) {
			pessoas = pessoaRepository.findPessoaBySexoPage(nomepesquisa, pesqsexo, pageable);

		} else {
			pessoas = pessoaRepository.findPessoaByNamePage(nomepesquisa, pageable);
		}

		ModelAndView modelAndView = new ModelAndView("cadastro/cadastropessoa");
		modelAndView.addObject("pessoas", pessoas);
		modelAndView.addObject("pessoaobj", new Pessoa());
		modelAndView.addObject("nomepesquisa", nomepesquisa);
		
		return modelAndView;

	}

	@GetMapping("**/pesquisarpessoa")
	public void imprimiPdf(@RequestParam("nomepesquisa") String nomepesquisa, @RequestParam("pesqsexo") String pesqsexo,
			HttpServletRequest request, HttpServletResponse response) throws Exception {

		List<Pessoa> pessoas = new ArrayList<Pessoa>();

		// Busca por Nome e Sexo
		if (pesqsexo != null && !pesqsexo.isEmpty() && nomepesquisa != null && !nomepesquisa.isEmpty()) {
			pessoas = pessoaRepository.findPessoaByNameSexo(nomepesquisa, pesqsexo);

			// Busca somente por Nome
		} else if (nomepesquisa != null && !nomepesquisa.isEmpty()) {
			pessoas = pessoaRepository.findPessoaByName(nomepesquisa);

			//Busca somente por Sexo
		}else if (pesqsexo != null && !pesqsexo.isEmpty()) {
			pessoas = pessoaRepository.findPessoaBySexo(pesqsexo);
		
			// Busca todos
	     }else {
			Iterable<Pessoa> iterator = pessoaRepository.findAll();
			for (Pessoa pessoa : iterator) {
				pessoas.add(pessoa);
			}
		}

		//Chama o serviço que faz a geração do PDF 
		byte[] pdf = reportUtil.gerarRelatorio(pessoas, "pessoa", request.getServletContext());

		//Tamanho da Resposta
		response.setContentLength(pdf.length);
		
		//Define na Resposta o tipo de Arquivo
		response.setContentType("application/octet-stream");
		
		//Cebeçalho da nossa Resposta, para o navegador interpretar
		String headerKey = "Content-Disposition";
		String headerValue = String.format("attachment; filename=\"%s\"", "relatorio.pdf");
		response.setHeader(headerKey, headerValue);
		
		//Finaliza a Resposta para o Navegador
		response.getOutputStream().write(pdf); 
		
	}

	@GetMapping("/telefones/{idpessoa}")
	public ModelAndView telefones(@PathVariable("idpessoa") Long idpessoa) {
		Optional<Pessoa> pessoa = pessoaRepository.findById(idpessoa);
		ModelAndView modelAndView = new ModelAndView("cadastro/telefones");
		modelAndView.addObject("pessoaobj", pessoa.get());
		// Podemos ver aqui que ao invés de pessoaid utilizou idpessoa, isso quer dizer
		// que o nome do objeto ao qual vc está instanciando não importa,
		// oque importa é que tipo de objeto vc está instanciando, há que classe ele
		// pertence?
		modelAndView.addObject("telefones", telefoneRepository.getTelefones(idpessoa));
		return modelAndView;
	}

	@PostMapping("**/addfonaPessoa/{pessoaid}")
	public ModelAndView addFonePessoa(Telefone telefone, @PathVariable("pessoaid") Long pessoaid) {

		Pessoa pessoa = pessoaRepository.findById(pessoaid).get();

		if (telefone != null && telefone.getNumero().isEmpty() || telefone.getTipo().isEmpty()) {

			ModelAndView modelAndView = new ModelAndView("cadastro/telefones");
			modelAndView.addObject("pessoaobj", pessoa);
			modelAndView.addObject("telefones", telefoneRepository.getTelefones(pessoaid));
			List<String> msg = new ArrayList<String>();
			if (telefone.getNumero().isEmpty()) {
				msg.add("Numero deve ser informado");
			}
			if (telefone.getTipo().isEmpty()) {
				msg.add("Tipo deve ser informado");
			}
			modelAndView.addObject("msg", msg);
			return modelAndView;
		}

		ModelAndView modelAndView = new ModelAndView("cadastro/telefones");

		telefone.setPessoa(pessoa);

		telefoneRepository.save(telefone);
		// Adiciona o retorno do objeto pai
		modelAndView.addObject("pessoaobj", pessoa);
		// Agora vamos carregar a lista de telefones, para a tela
		// Nosso método vai receber o ID da pessoa, se vc ver lá na Model vinculamos o
		// ID, como chave estrangeira da classe
		modelAndView.addObject("telefones", telefoneRepository.getTelefones(pessoaid));
		return modelAndView;
	}

	// Aqui a chamada na JSP foi interceptada
	@GetMapping("/removertelefone/{idtelefone}")
	// Aqui pegamos o ID do telefone selecionado
	public ModelAndView removertelefone(@PathVariable("idtelefone") Long idtelefone) {

		// Foi feito isso para podermos carregar a pessoa ao qual o Telefone irá estar
		// vinculada, ou seja temos que carregar o Objeto telefone e Pegar o IdPessoa
		// passando esse resultado para o pai Objeto (pessoa)
		Pessoa pessoa = telefoneRepository.findById(idtelefone).get().getPessoa();
		// Aqui deletamos o Telefone escolhido
		telefoneRepository.deleteById(idtelefone);

		// Retorna para mesma tela
		ModelAndView modelAndView = new ModelAndView("cadastro/telefones");
		// passa o Objeto pai para mostrar na tela
		modelAndView.addObject("pessoaobj", pessoa);

		// Estamos buscando o Telefone de determinada pessoa utilizando qual Parametro
		// ?? IdPessoa definido como chave estrangeira, ou seja, pegamos aquela
		// Pessoa pelo getId e carregamos sua Lista de Telefones (telefones)
		modelAndView.addObject("telefones", telefoneRepository.getTelefones(pessoa.getId()));
		return modelAndView;
	}

}
