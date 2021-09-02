package curso.springboot.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;


@Configuration
@EnableWebSecurity
public class WebConfigSecurity extends WebSecurityConfigurerAdapter{
	
	@Autowired
	private ImplementacaoUserDetailsService implementacaoUserDetailsService;
	
	@Override // Configura as solicitaões de acesso por HTTP
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf()
		.disable() //Desativa as configurações padrão de mémoria
		.authorizeRequests() //Permitir restringir acessos 
		.antMatchers(HttpMethod.GET, "/").permitAll() //Qualquer Usuário acessa a pagina inicial
		.antMatchers("/materialize/**").permitAll()
		.antMatchers(HttpMethod.GET, "/cadastropessoa").hasAnyRole("ADMIN")
		.anyRequest().authenticated()
		.and().formLogin().permitAll() //Permite qualquer Usuário, nesse caso o formulario de Login
		.loginPage("/login") //Qual é a pagina de Login
		.defaultSuccessUrl("/cadastropessoa") 
		.failureUrl("/login?error=true")
		.and().logout().logoutSuccessUrl("/login") //Mapeia a URL de Logout e invalida Usuario autenticado
		.logoutRequestMatcher(new AntPathRequestMatcher("logout"));	
	}
	
	@Override //Cria autenticação do Usuario com o Banco de Dados ou em Mémoria
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		
		auth.userDetailsService(implementacaoUserDetailsService).passwordEncoder(new BCryptPasswordEncoder());
		
		/*auth.inMemoryAuthentication().passwordEncoder(new BCryptPasswordEncoder())
		.withUser("gallego")
		.password("$2a$10$TbL6RZOtqQ5hEV8yJe9JRu7c/zy.5se.fpQfgMvrz6s72CLM/CvEa")
		.roles("ADMIN");*/
	}
	

	@Override //Ignora URL especificas
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("**/materialize/**");
	}

	

}
