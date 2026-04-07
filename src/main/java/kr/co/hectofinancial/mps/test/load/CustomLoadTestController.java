package kr.co.hectofinancial.mps.test.load;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class CustomLoadTestController {
    private final CustomLoadTestService customLoadTestService;

    @GetMapping("/custom/load/test")
    public String startLoadTest(@RequestParam(value = "target") int target, @RequestParam("count")int count ){
        return customLoadTestService.startLoadTest(target, count);
    }

}
