package com.legostudy.main;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import com.legostudy.account.AccountRepository;
import com.legostudy.account.AccountService;
import com.legostudy.account.SignUpForm;

@SpringBootTest
@AutoConfigureMockMvc
public class MainControllerTest {
	
	@Autowired
	MockMvc mockMvc;
	@Autowired
	AccountService accountService;
	@Autowired
	AccountRepository accountRepository;
	
	@BeforeEach
	void beforeEach() {
		SignUpForm signUpForm = new SignUpForm();
		signUpForm.setNickname("testid");
		signUpForm.setEmail("testemail@email.com");
		signUpForm.setPassword("12345678");
		accountService.processNewAccount(signUpForm);
	}
	
	@AfterEach
	void afterEach() {
		accountRepository.deleteAll();
	}
	
	@DisplayName("이메일로 로그인 성공")
	@Test
	void login_with_email() throws Exception{
		mockMvc.perform(post("/login")
				.param("username", "testemail@email.com")
				.param("password", "12345678")
				.with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/"))
				.andExpect(authenticated().withUsername("testid"));
	}
	
	@DisplayName("닉네임으로 로그인 성공")
	@Test
	void login_with_nickname() throws Exception{
		mockMvc.perform(post("/login")
				.param("username", "testid")
				.param("password", "12345678")
				.with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/"))
				.andExpect(authenticated().withUsername("testid"));
	}
	
	@DisplayName("로그인 실패")
	@Test
	void login_fail() throws Exception{
		mockMvc.perform(post("/login")
				.param("username", "123123")
				.param("password", "52434643234")
				.with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login?error"))
				.andExpect(unauthenticated());
	}
	
	@DisplayName("로그아웃")
	@Test
	void logout() throws Exception{
		mockMvc.perform(post("/logout")
				.with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/"))
				.andExpect(unauthenticated());
	}
}
