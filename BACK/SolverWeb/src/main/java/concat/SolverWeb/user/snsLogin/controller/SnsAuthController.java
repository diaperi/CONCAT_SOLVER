package concat.SolverWeb.user.snsLogin.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import concat.SolverWeb.user.snsLogin.dto.SnsUserDTO;
import concat.SolverWeb.user.snsLogin.service.SnsUserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

@Controller
@RequestMapping("/snslogin")
public class SnsAuthController {

    @Autowired
    private SnsUserService snsUserService;

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

            // 세션에 사용자 정보 저장
            session.setAttribute("user", snsUserDTO);

            // 메인 페이지로 리다이렉트
            return "hyeeun/mainpage"; // 메인 페이지 경로
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/error"; // 오류 페이지 경로
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
}







