package io.spring.cloud.samples.animalrescue.backend;

import io.spring.cloud.samples.animalrescue.support.htmlunit.server.WebTestClientHtmlUnitDriverBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@WebFluxTest(controllers = UserController.class)
@Import(SecurityConfiguration.class)
class UserControllerTest {

	@MockBean
	AdoptionService adoptionService;

	@Autowired
	WebTestClient webTestClient;

	private WebDriver webDriver;

	@BeforeEach
	void setUp() {
		this.webDriver = WebTestClientHtmlUnitDriverBuilder.webTestClientSetup(webTestClient).build();
	}

	@Test
	void shouldNotAllowAccessForUnauthenticatedUsers() {
		webDriver.navigate().to("http://localhost/whoami");

		assertThat(webDriver.getCurrentUrl()).endsWith("/login");
	}

	@Test
	void shouldDisplayUsernameAfterSuccessfulLogin() {
		String testUserName = "bob";

		webDriver.navigate().to("http://localhost/whoami");
		webDriver.findElement(By.name("username")).sendKeys(testUserName);
		webDriver.findElement(By.name("password")).sendKeys("test");
		webDriver.findElement(By.className("btn-primary")).click();

		assertThat(webDriver.getPageSource()).contains(testUserName);
	}

	@Test
	void shouldDisplayErrorForFailedLogin() {
		String testUserName = "bob";

		webDriver.navigate().to("http://localhost/whoami");
		webDriver.findElement(By.name("username")).sendKeys(testUserName);
		webDriver.findElement(By.name("password")).sendKeys("incorrect password");
		webDriver.findElement(By.className("btn-primary")).click();

		assertThat(webDriver.getPageSource()).contains("Invalid credentials");
	}

}