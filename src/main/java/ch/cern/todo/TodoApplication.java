package ch.cern.todo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import javax.sql.DataSource;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;
import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.H2;

@SpringBootApplication
public class TodoApplication {

	public static void main(String[] args) {
		SpringApplication.run(TodoApplication.class, args);
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		// Remove security on H2 console links
		// Source: https://github.com/spring-projects/spring-security/issues/12546#issuecomment-1700917846
		return http
				.csrf(csrf -> csrf
					.ignoringRequestMatchers(toH2Console())
					.disable()
				)
				.authorizeHttpRequests(auth -> auth.requestMatchers(toH2Console()).permitAll())
				.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
				.build();
	}

	@Bean
	DataSource dataSource() {
		return new EmbeddedDatabaseBuilder()
				.setType(H2)
				.addScripts(JdbcDaoImpl.DEFAULT_USER_SCHEMA_DDL_LOCATION)
				.build();
	}

	@Bean
	UserDetailsManager users(DataSource dataSource) {
		UserDetails user = User.builder()
				.username("user")
				.password("{noop}u1pass")
				.roles("USER")
				.build();
		UserDetails userTwo = User.builder()
				.username("userTwo")
				.password("{noop}u2pass")
				.roles("USER")
				.build();
		UserDetails admin = User.builder()
				.username("admin")
				.password("{noop}admin")
				.roles("USER", "ADMIN")
				.build();
		JdbcUserDetailsManager users = new JdbcUserDetailsManager(dataSource);
		users.createUser(user);
		users.createUser(userTwo);
		users.createUser(admin);
		return users;
	}
}
