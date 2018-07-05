package com.jielong.base.config;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import com.jielong.core.service.AdminService;


@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter{

	@Autowired
	AdminService  adminService;
	//user
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {		
	
		 auth.userDetailsService(adminService);  
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable()
		  .formLogin()
		  .loginPage("/login").defaultSuccessUrl("/home").failureUrl("/loginError")
		  .and()
		  .rememberMe().tokenValiditySeconds(2419200).key("jielongKey")         //cookie中的jielongKey保存四周时间
		  .and()
		  .authorizeRequests()
		  .antMatchers("/view/**").authenticated()
		  .antMatchers("/home").authenticated()
		  .antMatchers("/").authenticated()
		  .anyRequest().permitAll();
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		super.configure(web);
	
	}
	
	
	
      
}
