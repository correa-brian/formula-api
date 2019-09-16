package formula.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import formula.api.user.User;

@RestController
public class RootController {
	
	@RequestMapping("/")
	public void run() {
		System.out.println("execute run");
	}
}
