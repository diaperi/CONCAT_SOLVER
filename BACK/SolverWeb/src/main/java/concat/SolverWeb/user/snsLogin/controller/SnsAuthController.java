//package concat.SolverWeb.user.snsLogin.controller;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import concat.SolverWeb.user.snsLogin.dto.SnsUserDTO;
//import concat.SolverWeb.user.snsLogin.service.SnsUserService;
//import jakarta.servlet.http.HttpSession;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.client.RestTemplate;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//
//@Controller
//@RequiredArgsConstructor
//@RequestMapping("/snslogin")
//public class SnsAuthController {
//
//    @Autowired
//    private SnsUserService snsUserService;
//
//    @Autowired
//    private HttpSession session;  // HttpSession 주입
//
//    private static final Logger logger = LoggerFactory.getLogger(SnsAuthController.class);
//
//
//    @Value("${oauth2.client.registration.google.client-id}")
//    private String googleClientId;
//
//    @Value("${oauth2.client.registration.google.client-secret}")
//    private String googleClientSecret;
//
//    @Value("${oauth2.client.registration.google.redirect-uri}")
//    private String googleRedirectUri;
//
//    @Value("${oauth2.client.registration.kakao.client-id}")
//    private String kakaoClientId;
//
//    @Value("${oauth2.client.registration.kakao.client-secret}")
//    private String kakaoClientSecret;
//
//    @Value("${oauth2.client.registration.kakao.redirect-uri}")
//    private String kakaoRedirectUri;
//
//    @Value("${oauth2.client.registration.naver.client-id}")
//    private String naverClientId;
//
//    @Value("${oauth2.client.registration.naver.client-secret}")
//    private String naverClientSecret;
//
//    @Value("${oauth2.client.registration.naver.redirect-uri}")
//    private String naverRedirectUri;
//
//    private final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
//    private final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";
//    private final String NAVER_TOKEN_URL = "https://nid.naver.com/oauth2.0/token";
//
//    private final String GOOGLE_AUTHORIZATION_URL = "https://accounts.google.com/o/oauth2/v2/auth";
//    private final String KAKAO_AUTHORIZATION_URL = "https://kauth.kakao.com/oauth/authorize";
//    private final String NAVER_AUTHORIZATION_URL = "https://nid.naver.com/oauth2.0/authorize";
//
//    private final String GOOGLE_LOGOUT_URL = "https://accounts.google.com/o/oauth2/revoke?token=";
//    private final String KAKAO_LOGOUT_URL = "https://kapi.kakao.com/v1/user/logout";
//    private final String NAVER_LOGOUT_URL = "https://nid.naver.com/oauth2.0/token";
//
//    @GetMapping("/oauth2/authorization/google")
//    public String googleLogin() {
//        return "redirect:" + GOOGLE_AUTHORIZATION_URL + "?client_id=" + googleClientId +
//                "&redirect_uri=" + googleRedirectUri + "&response_type=code&scope=email profile";
//    }
//
//    @GetMapping("/oauth2/authorization/kakao")
//    public String kakaoLogin() {
//        return "redirect:" + KAKAO_AUTHORIZATION_URL + "?client_id=" + kakaoClientId +
//                "&redirect_uri=" + kakaoRedirectUri + "&response_type=code";
//    }
//
//    @GetMapping("/oauth2/authorization/naver")
//    public String naverLogin() {
//        return "redirect:" + NAVER_AUTHORIZATION_URL + "?client_id=" + naverClientId +
//                "&redirect_uri=" + naverRedirectUri + "&response_type=code";
//    }
//
//    @GetMapping("/oauth2/code/google")
//    public String googleAuth(@RequestParam String code, Model model, HttpSession session) {
//        return processSnsAuth("google", code, model, session);
//    }
//
//    @GetMapping("/oauth2/code/kakao")
//    public String kakaoAuth(@RequestParam String code, Model model, HttpSession session) {
//        return processSnsAuth("kakao", code, model, session);
//    }
//
//    @GetMapping("/oauth2/code/naver")
//    public String naverAuth(@RequestParam String code, Model model, HttpSession session) {
//        return processSnsAuth("naver", code, model, session);
//    }
//
//    private String processSnsAuth(String provider, String code, Model model, HttpSession session) {
//        try {
//            String accessToken = getAccessToken(provider, code);
//            String userInfo = getUserInfo(provider, accessToken);
//            SnsUserDTO snsUserDTO = parseUserInfo(userInfo, provider);
//
//            snsUserService.saveOrUpdateUser(snsUserDTO);
//
//            // 세션에 사용자 정보와 로그인 유형 저장
//            session.setAttribute("user", snsUserDTO);
//            session.setAttribute("loginType", "SNS");
//
//            return "redirect:/main/mainPage";
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "redirect:/error";
//        }
//    }
//
//
//    @GetMapping("/main")
//    public String mainPage(HttpSession session, Model model) {
//        SnsUserDTO snsUser = (SnsUserDTO) session.getAttribute("user");
//        if (snsUser == null) {
//            return "redirect:/user/login";
//        }
//
//        model.addAttribute("user", snsUser);
//        return "hyeeun/mainpage"; // 메인 페이지
//    }
//
//
//    private String getAccessToken(String provider, String code) {
//        String tokenUrl;
//        String clientId;
//        String clientSecret;
//        String redirectUri;
//
//        switch (provider) {
//            case "google":
//                tokenUrl = GOOGLE_TOKEN_URL;
//                clientId = googleClientId;
//                clientSecret = googleClientSecret;
//                redirectUri = googleRedirectUri;
//                break;
//            case "kakao":
//                tokenUrl = KAKAO_TOKEN_URL;
//                clientId = kakaoClientId;
//                clientSecret = kakaoClientSecret;
//                redirectUri = kakaoRedirectUri;
//                break;
//            case "naver":
//                tokenUrl = NAVER_TOKEN_URL;
//                clientId = naverClientId;
//                clientSecret = naverClientSecret;
//                redirectUri = naverRedirectUri;
//                break;
//            default:
//                throw new IllegalArgumentException("Unsupported provider: " + provider);
//        }
//
//        RestTemplate restTemplate = new RestTemplate();
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);
//
//        String requestBody = "grant_type=authorization_code&code=" + code +
//                "&redirect_uri=" + redirectUri +
//                "&client_id=" + clientId +
//                "&client_secret=" + clientSecret;
//
//        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
//
//        ResponseEntity<String> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, request, String.class);
//
//        if (response.getStatusCode() == HttpStatus.OK) {
//            ObjectMapper mapper = new ObjectMapper();
//            try {
//                JsonNode root = mapper.readTree(response.getBody());
//                return root.path("access_token").asText();
//            } catch (Exception e) {
//                throw new RuntimeException("Failed to parse token response", e);
//            }
//        } else {
//            throw new RuntimeException("Failed to obtain access token. HTTP Status: " + response.getStatusCode());
//        }
//    }
//
//    private String getUserInfo(String provider, String accessToken) {
//        String userInfoUrl;
//        switch (provider) {
//            case "google":
//                userInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo?access_token=" + accessToken;
//                break;
//            case "kakao":
//                userInfoUrl = "https://kapi.kakao.com/v2/user/me";
//                break;
//            case "naver":
//                userInfoUrl = "https://openapi.naver.com/v1/nid/me";
//                break;
//            default:
//                throw new IllegalArgumentException("Unsupported provider: " + provider);
//        }
//
//        RestTemplate restTemplate = new RestTemplate();
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "Bearer " + accessToken);
//
//        HttpEntity<String> request = new HttpEntity<>(headers);
//
//        ResponseEntity<String> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, request, String.class);
//
//        if (response.getStatusCode() == HttpStatus.OK) {
//            return response.getBody();
//        } else {
//            throw new RuntimeException("Failed to obtain user info. HTTP Status: " + response.getStatusCode());
//        }
//    }
//
//    private SnsUserDTO parseUserInfo(String userInfoResponse, String provider) throws Exception {
//        ObjectMapper mapper = new ObjectMapper();
//        JsonNode root = mapper.readTree(userInfoResponse);
//
//        SnsUserDTO userDTO = new SnsUserDTO();
//        userDTO.setEmail(parseEmail(root, provider));
//        userDTO.setName(parseName(root, provider));
//        userDTO.setProvider(provider);
//        userDTO.setProviderId(parseProviderId(root, provider));
//
//        return userDTO;
//    }
//
//    private String parseEmail(JsonNode root, String provider) {
//        switch (provider) {
//            case "google":
//                return root.path("email").asText();
//            case "kakao":
//                return root.path("kakao_account").path("email").asText();
//            case "naver":
//                return root.path("response").path("email").asText();
//            default:
//                throw new IllegalArgumentException("Unsupported provider: " + provider);
//        }
//    }
//
//    private String parseName(JsonNode root, String provider) {
//        switch (provider) {
//            case "google":
//                return root.path("name").asText();
//            case "kakao":
//                return root.path("kakao_account").path("profile").path("nickname").asText();
//            case "naver":
//                return root.path("response").path("name").asText();
//            default:
//                throw new IllegalArgumentException("Unsupported provider: " + provider);
//        }
//    }
//
//    private String parseProviderId(JsonNode root, String provider) {
//        switch (provider) {
//            case "google":
//                return root.path("sub").asText();
//            case "kakao":
//                return root.path("id").asText();
//            case "naver":
//                return root.path("response").path("id").asText();
//            default:
//                throw new IllegalArgumentException("Unsupported provider: " + provider);
//        }
//    }
//
//    @GetMapping("/sns-update")
//    public String updateSnsUserDetail(HttpSession session, Model model) {
//        // 세션에서 사용자 정보와 로그인 유형을 가져옵니다.
//        SnsUserDTO snsUser = (SnsUserDTO) session.getAttribute("user");
//        String loginType = (String) session.getAttribute("loginType");
//
//        // 사용자가 로그인되지 않은 경우 로그인 페이지로 리디렉션
//        if (snsUser == null) {
//            return "redirect:/user/login";
//        }
//
//        // 로그인 유형에 따라 다른 업데이트 페이지로 리디렉션
//        if ("SNS".equals(loginType)) {
//            model.addAttribute("user", snsUser);
//            return "hyeeun/snslogin/hyeeunsnsupdate"; // SNS 사용자 업데이트 폼
//        } else if ("GENERAL".equals(loginType)) {
//            model.addAttribute("updateUser");
//            return "hyeeun/userchange/hyeeunupdate"; // 일반 사용자 업데이트 폼
//        }
//
//        // 로그인 유형이 정의되지 않은 경우 로그인 페이지로 리디렉션
//        return "redirect:/user/login";
//    }
//
//    @PostMapping("/delete/{providerId}")
//    public ResponseEntity<String> deleteUserByProviderId(@PathVariable String providerId, HttpSession session) {
//        try {
//            if (providerId == null || providerId.trim().isEmpty()) {
//                return ResponseEntity.badRequest().body("Invalid provider ID");
//            }
//
//            // 서비스 호출 시 providerId를 전달
//            snsUserService.deleteUserByProviderId(providerId);
//
//            // 세션 무효화
//            session.invalidate();
//            return ResponseEntity.ok("ok");
//        } catch (Exception e) {
//            e.printStackTrace(); // 에러 로그
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("fail");
//        }
//    }
//
//    @GetMapping("/logout")
//    public String logout(HttpSession session) {
//        String accessToken = (String) session.getAttribute("accessToken");
//        String provider = (String) session.getAttribute("loginType");
//
//        if (accessToken != null && provider != null) {
//            RestTemplate restTemplate = new RestTemplate();
//            HttpHeaders headers = new HttpHeaders();
//            headers.set("Authorization", "Bearer " + accessToken);
//
//            try {
//                if ("google".equals(provider)) {
//                    ResponseEntity<String> response = restTemplate.postForEntity(GOOGLE_LOGOUT_URL + accessToken, null, String.class);
//                    if (!response.getStatusCode().is2xxSuccessful()) {
//                        throw new RuntimeException("Google logout failed. HTTP Status: " + response.getStatusCode());
//                    }
//                } else if ("kakao".equals(provider)) {
//                    HttpEntity<String> request = new HttpEntity<>(headers);
//                    ResponseEntity<String> response = restTemplate.exchange(KAKAO_LOGOUT_URL, HttpMethod.POST, request, String.class);
//                    if (!response.getStatusCode().is2xxSuccessful()) {
//                        throw new RuntimeException("Kakao logout failed. HTTP Status: " + response.getStatusCode());
//                    }
//                } else if ("naver".equals(provider)) {
//                    String naverLogoutUrl = NAVER_LOGOUT_URL + "?grant_type=delete&client_id=" + naverClientId + "&client_secret=" + naverClientSecret + "&access_token=" + accessToken + "&service_provider=NAVER";
//                    ResponseEntity<String> response = restTemplate.postForEntity(naverLogoutUrl, null, String.class);
//                    if (!response.getStatusCode().is2xxSuccessful()) {
//                        throw new RuntimeException("Naver logout failed. HTTP Status: " + response.getStatusCode());
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//        if (session != null) {
//            System.out.println("세션 무효화 시도 중...");
//            session.invalidate();
//            System.out.println("세션이 무효화되었습니다.");
//        } else {
//            System.out.println("세션이 null입니다.");
//        }
//
//        try {
//            session.getAttribute("loginType");
//        } catch (IllegalStateException e) {
//            System.out.println("세션이 성공적으로 무효화되었습니다.");
//        }
//
//
//        return "redirect:/user/login"; // 로그아웃 후 로그인 페이지로 리디렉션
//    }
//}
//
//
//
//
//
//
package concat.SolverWeb.user.snsLogin.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import concat.SolverWeb.user.snsLogin.dto.SnsUserDTO;
import concat.SolverWeb.user.snsLogin.service.SnsUserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequiredArgsConstructor
@RequestMapping("/snslogin")
public class SnsAuthController {

    @Autowired
    private SnsUserService snsUserService;

    @Autowired
    private HttpSession session;  // HttpSession 주입

    private static final Logger logger = LoggerFactory.getLogger(SnsAuthController.class);

    @Value("${oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    @Value("${oauth2.client.registration.google.redirect-uri}")
    private String googleRedirectUri;

    @Value("${oauth2.client.registration.kakao.client-id}")
    private String kakaoClientId;

    @Value("${oauth2.client.registration.kakao.client-secret}")
    private String kakaoClientSecret;

    @Value("${oauth2.client.registration.kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Value("${oauth2.client.registration.naver.client-id}")
    private String naverClientId;

    @Value("${oauth2.client.registration.naver.client-secret}")
    private String naverClientSecret;

    @Value("${oauth2.client.registration.naver.redirect-uri}")
    private String naverRedirectUri;

    private final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private final String NAVER_TOKEN_URL = "https://nid.naver.com/oauth2.0/token";

    private final String GOOGLE_AUTHORIZATION_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private final String KAKAO_AUTHORIZATION_URL = "https://kauth.kakao.com/oauth/authorize";
    private final String NAVER_AUTHORIZATION_URL = "https://nid.naver.com/oauth2.0/authorize";

    private final String GOOGLE_LOGOUT_URL = "https://accounts.google.com/o/oauth2/revoke?token=";
    private final String KAKAO_LOGOUT_URL = "https://kapi.kakao.com/v1/user/logout";
    private final String NAVER_LOGOUT_URL = "https://nid.naver.com/oauth2.0/token";

    @GetMapping("/oauth2/authorization/google")
    public String googleLogin() {
        return "redirect:" + GOOGLE_AUTHORIZATION_URL + "?client_id=" + googleClientId +
                "&redirect_uri=" + googleRedirectUri + "&response_type=code&scope=email profile";
    }

    @GetMapping("/oauth2/authorization/kakao")
    public String kakaoLogin() {
        return "redirect:" + KAKAO_AUTHORIZATION_URL + "?client_id=" + kakaoClientId +
                "&redirect_uri=" + kakaoRedirectUri + "&response_type=code";
    }

    @GetMapping("/oauth2/authorization/naver")
    public String naverLogin() {
        return "redirect:" + NAVER_AUTHORIZATION_URL + "?client_id=" + naverClientId +
                "&redirect_uri=" + naverRedirectUri + "&response_type=code";
    }

    @GetMapping("/oauth2/code/google")
    public String googleAuth(@RequestParam String code, Model model, HttpSession session) {
        return processSnsAuth("google", code, model, session);
    }

    @GetMapping("/oauth2/code/kakao")
    public String kakaoAuth(@RequestParam String code, Model model, HttpSession session) {
        return processSnsAuth("kakao", code, model, session);
    }

    @GetMapping("/oauth2/code/naver")
    public String naverAuth(@RequestParam String code, Model model, HttpSession session) {
        return processSnsAuth("naver", code, model, session);
    }

    private String processSnsAuth(String provider, String code, Model model, HttpSession session) {
        try {
            String accessToken = getAccessToken(provider, code);
            String userInfo = getUserInfo(provider, accessToken);
            SnsUserDTO snsUserDTO = parseUserInfo(userInfo, provider);

            snsUserService.saveOrUpdateUser(snsUserDTO);

            // 세션에 사용자 정보와 로그인 유형 저장
            session.setAttribute("loggedInUser", snsUserDTO);  // 주의: 여기에서 "loggedInUser"로 저장합니다.
            session.setAttribute("loginType", "SNS");

            logger.info("세션에 저장된 SNS 사용자 정보: {}", snsUserDTO);

            return "redirect:/main/mainPage";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/error";
        }
    }


    private String getAccessToken(String provider, String code) {
        String tokenUrl;
        String clientId;
        String clientSecret;
        String redirectUri;

        switch (provider) {
            case "google":
                tokenUrl = GOOGLE_TOKEN_URL;
                clientId = googleClientId;
                clientSecret = googleClientSecret;
                redirectUri = googleRedirectUri;
                break;
            case "kakao":
                tokenUrl = KAKAO_TOKEN_URL;
                clientId = kakaoClientId;
                clientSecret = kakaoClientSecret;
                redirectUri = kakaoRedirectUri;
                break;
            case "naver":
                tokenUrl = NAVER_TOKEN_URL;
                clientId = naverClientId;
                clientSecret = naverClientSecret;
                redirectUri = naverRedirectUri;
                break;
            default:
                throw new IllegalArgumentException("Unsupported provider: " + provider);
        }

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);

        String requestBody = "grant_type=authorization_code&code=" + code +
                "&redirect_uri=" + redirectUri +
                "&client_id=" + clientId +
                "&client_secret=" + clientSecret;

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, request, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                JsonNode root = mapper.readTree(response.getBody());
                return root.path("access_token").asText();
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse token response", e);
            }
        } else {
            throw new RuntimeException("Failed to obtain access token. HTTP Status: " + response.getStatusCode());
        }
    }

    private String getUserInfo(String provider, String accessToken) {
        String userInfoUrl;
        switch (provider) {
            case "google":
                userInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo?access_token=" + accessToken;
                break;
            case "kakao":
                userInfoUrl = "https://kapi.kakao.com/v2/user/me";
                break;
            case "naver":
                userInfoUrl = "https://openapi.naver.com/v1/nid/me";
                break;
            default:
                throw new IllegalArgumentException("Unsupported provider: " + provider);
        }

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, request, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to obtain user info. HTTP Status: " + response.getStatusCode());
        }
    }

    private SnsUserDTO parseUserInfo(String userInfoResponse, String provider) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(userInfoResponse);

        SnsUserDTO userDTO = new SnsUserDTO();
        userDTO.setEmail(parseEmail(root, provider));
        userDTO.setName(parseName(root, provider));
        userDTO.setProvider(provider);
        userDTO.setProviderId(parseProviderId(root, provider));

        return userDTO;
    }

    private String parseEmail(JsonNode root, String provider) {
        switch (provider) {
            case "google":
                return root.path("email").asText();
            case "kakao":
                return root.path("kakao_account").path("email").asText();
            case "naver":
                return root.path("response").path("email").asText();
            default:
                throw new IllegalArgumentException("Unsupported provider: " + provider);
        }
    }

    private String parseName(JsonNode root, String provider) {
        switch (provider) {
            case "google":
                return root.path("name").asText();
            case "kakao":
                return root.path("kakao_account").path("profile").path("nickname").asText();
            case "naver":
                return root.path("response").path("name").asText();
            default:
                throw new IllegalArgumentException("Unsupported provider: " + provider);
        }
    }

    private String parseProviderId(JsonNode root, String provider) {
        switch (provider) {
            case "google":
                return root.path("sub").asText();
            case "kakao":
                return root.path("id").asText();
            case "naver":
                return root.path("response").path("id").asText();
            default:
                throw new IllegalArgumentException("Unsupported provider: " + provider);
        }
    }

    @GetMapping("/sns-update")
    public String updateSnsUserDetail(HttpSession session, Model model) {
        // 세션에서 사용자 정보와 로그인 유형을 가져옵니다.
        SnsUserDTO snsUser = (SnsUserDTO) session.getAttribute("loggedInUser");
        String loginType = (String) session.getAttribute("loginType");

        // 사용자가 로그인되지 않은 경우 로그인 페이지로 리디렉션
        if (snsUser == null) {
            return "redirect:/user/login";
        }

        // 로그인 유형에 따라 다른 업데이트 페이지로 리디렉션
        if ("SNS".equals(loginType)) {
            model.addAttribute("user", snsUser);
            return "hyeeun/snslogin/hyeeunsnsupdate"; // SNS 사용자 업데이트 폼
        } else if ("GENERAL".equals(loginType)) {
            model.addAttribute("updateUser");
            return "hyeeun/userchange/hyeeunupdate"; // 일반 사용자 업데이트 폼
        }

        // 로그인 유형이 정의되지 않은 경우 로그인 페이지로 리디렉션
        return "redirect:/user/login";
    }

    @PostMapping("/delete/{providerId}")
    public ResponseEntity<String> deleteUserByProviderId(@PathVariable String providerId, HttpSession session) {
        try {
            if (providerId == null || providerId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Invalid provider ID");
            }

            // 서비스 호출 시 providerId를 전달
            snsUserService.deleteUserByProviderId(providerId);

            // 세션 무효화
            session.invalidate();
            return ResponseEntity.ok("ok");
        } catch (Exception e) {
            e.printStackTrace(); // 에러 로그
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("fail");
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        String accessToken = (String) session.getAttribute("accessToken");
        String provider = (String) session.getAttribute("loginType");

        if (accessToken != null && provider != null) {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);

            try {
                if ("google".equals(provider)) {
                    ResponseEntity<String> response = restTemplate.postForEntity(GOOGLE_LOGOUT_URL + accessToken, null, String.class);
                    if (!response.getStatusCode().is2xxSuccessful()) {
                        throw new RuntimeException("Google logout failed. HTTP Status: " + response.getStatusCode());
                    }
                } else if ("kakao".equals(provider)) {
                    HttpEntity<String> request = new HttpEntity<>(headers);
                    ResponseEntity<String> response = restTemplate.exchange(KAKAO_LOGOUT_URL, HttpMethod.POST, request, String.class);
                    if (!response.getStatusCode().is2xxSuccessful()) {
                        throw new RuntimeException("Kakao logout failed. HTTP Status: " + response.getStatusCode());
                    }
                } else if ("naver".equals(provider)) {
                    String naverLogoutUrl = NAVER_LOGOUT_URL + "?grant_type=delete&client_id=" + naverClientId + "&client_secret=" + naverClientSecret + "&access_token=" + accessToken + "&service_provider=NAVER";
                    ResponseEntity<String> response = restTemplate.postForEntity(naverLogoutUrl, null, String.class);
                    if (!response.getStatusCode().is2xxSuccessful()) {
                        throw new RuntimeException("Naver logout failed. HTTP Status: " + response.getStatusCode());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (session != null) {
            session.invalidate();
        }

        return "redirect:/user/login"; // 로그아웃 후 로그인 페이지로 리디렉션
    }
}
