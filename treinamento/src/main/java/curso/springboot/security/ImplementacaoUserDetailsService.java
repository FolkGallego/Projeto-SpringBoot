package curso.springboot.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import curso.springboot.model.Usuario;
import curso.springboot.repository.UsuarioRepository;

@Service
@Transactional
public class ImplementacaoUserDetailsService implements UserDetailsService{
	
	@Autowired
	private UsuarioRepository usuarioRepository;
	

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
	Usuario usuario = usuarioRepository.findUserByLogin(username);
	
	if(usuario == null) {
		throw new UsernameNotFoundException("Usuario Não foi Encontrado!!");
		}
	
	return new User(usuario.getLogin(), usuario.getPassword(),
			usuario.isEnabled(), true, 
			//Na hora que iniciarmos o Spring vai carregar as autoridades que cadastramos, Tabela N*N
			true, true, usuario.getAuthorities());
	}

}
