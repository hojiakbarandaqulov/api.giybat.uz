package api.giybat.uz;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest
class ApplicationTests {

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Test
	void contextLoads() {
		System.out.println(bCryptPasswordEncoder.encode("123456"));
	}
}
