package com.legostudy.account;

import java.util.List;

import com.legostudy.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService implements UserDetailsService{

    private final AccountRepository accountRepository;
    private final JavaMailSender javaMailSender;
    private final PasswordEncoder passwordEncoder;
    // private final AuthenticationManager authenticationManager;

    @Transactional // 트랜젝션을 걸어둬야 EmailCheckToken이 먹힘 그래야 newAccount가 detached 상태가아닌 persistent 상태로 됨
    public Account processNewAccount(SignUpForm signUpForm) {
        Account newAccount = saveNewAccount(signUpForm);
        newAccount.generateEmailCheckToken();
        sendSignUpConfirmEmail(newAccount);
        return newAccount;
    }

    private Account saveNewAccount(SignUpForm signUpForm) {
        Account account = Account.builder()
                .email(signUpForm.getEmail())
                .nickname(signUpForm.getNickname())
                // .password(signUpForm.getPassword()) // Need Pwd encoding !!!
                .password(passwordEncoder.encode(signUpForm.getPassword()))
                .studyCreatedByWeb(true)
                .studyEnrollmentResultByWeb(true)
                .studyUpdatedByWeb(true)
                .build();
        Account newAccount = accountRepository.save(account);
        return newAccount;
    }

    public void sendSignUpConfirmEmail(Account newAccount) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(newAccount.getEmail());
        mailMessage.setSubject("레고스터디, 회원 가입 안내");
        mailMessage.setText("/check-email-token?token=" + newAccount.getEmailCheckToken() +
                "&email=" + newAccount.getEmail() + "");
        javaMailSender.send(mailMessage);
    }

    public void login(Account account) {
        // 아래 2row가 정석이지만 plainText Pwd를 사용하지않으므로 밑 방법 사용
        // UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(account.getNickname(), account.getPassword());
        // Authentication authentication = authenticationManager.authenticate(token);


        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                    new UserAccount(account),
                    account.getPassword(),
                    List.of(new SimpleGrantedAuthority("ROLE_USER")));

        SecurityContext context =  SecurityContextHolder.getContext();
        context.setAuthentication(token);
    }

	@Override
	public UserDetails loadUserByUsername(String emailOrNickname) throws UsernameNotFoundException {
		Account account = accountRepository.findByEmail(emailOrNickname);
		if(account == null) {
			account = accountRepository.findByNickname(emailOrNickname);
		}
		if(account == null) {
			throw new UsernameNotFoundException(emailOrNickname);
		}
		return new UserAccount(account);
	}
}
