package com.example.demo.service;

import com.example.demo.models.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class PeerService {
    private final WebClient.Builder webClient;

    private String url = "https://edu-api.21-school.ru/services/21-school/api/v1";
    private String accessToken = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJ5V29landCTmxROWtQVEpFZnFpVzRrc181Mk1KTWkwUHl2RHNKNlgzdlFZIn0.eyJleHAiOjE3MzQ1NjQwODMsImlhdCI6MTczNDUyODA4MywiYXV0aF90aW1lIjoxNzM0NTI4MDgzLCJqdGkiOiJmZWExNjFkOS1kY2JiLTQ1ODEtOWVlNy1lZTE5N2UxNjFjMzYiLCJpc3MiOiJodHRwczovL2F1dGguc2JlcmNsYXNzLnJ1L2F1dGgvcmVhbG1zL0VkdVBvd2VyS2V5Y2xvYWsiLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiNTJkYzRmYTQtOTYyYy00OGI4LWJiYmMtOGUzOWRlODY5MTY3IiwidHlwIjoiQmVhcmVyIiwiYXpwIjoic2Nob29sMjEiLCJub25jZSI6IjJiYWI5ZDU5LTI0Y2UtNDdmZC04YmU3LTJlNzZjZDg3MDVkZiIsInNlc3Npb25fc3RhdGUiOiI5ZWNjYTI3MC03NjE4LTQyZWItOTQ5OS04YjA1MDg5ODkwNTciLCJhY3IiOiIxIiwiYWxsb3dlZC1vcmlnaW5zIjpbImh0dHBzOi8vZWR1LjIxLXNjaG9vbC5ydSIsImh0dHBzOi8vZWR1LWFkbWluLjIxLXNjaG9vbC5ydSJdLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsiZGVmYXVsdC1yb2xlcy1lZHVwb3dlcmtleWNsb2FrIiwib2ZmbGluZV9hY2Nlc3MiLCJ1bWFfYXV0aG9yaXphdGlvbiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoib3BlbmlkIHByb2ZpbGUgZW1haWwiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwidXNlcl9pZCI6IjlmNDI5NjVlLTMxZTQtNGE3MS1iMzZiLTVjNWJjMDYxYjU5ZSIsIm5hbWUiOiJNYWN5IEdhYnJpZWxsZSIsImF1dGhfdHlwZV9jb2RlIjoiZGVmYXVsdCIsInByZWZlcnJlZF91c2VybmFtZSI6Im1hY3lnYWJyIiwiZ2l2ZW5fbmFtZSI6Ik1hY3kiLCJmYW1pbHlfbmFtZSI6IkdhYnJpZWxsZSIsImVtYWlsIjoibWFjeWdhYnJAc3R1ZGVudC4yMS1zY2hvb2wucnUifQ.qwn1I8CIyWPch7tPVxudG-dIsFXTM99GKuoZDwxJKmCE4GXW6KvVomLNRYynSY8oBxjjQUqAyXd2gFrTrWWs5ElL8SCkTz7JUKulzbdcrwseh1Xv9ZRPk_X3IGp-Zod3RdqUqeZauAFPGf3A1yxGX4ytWaMZ-VtgXlgGf_IUAIfgQAtRpMD_7u9FFxvo1Wn4QFuwV_kMw5S-I8omewt2BwbIz-YsTH29kL43eJCLO3yY8AfVv8vhU8rJRU3L9rxR5B61Gwc5vNMzup50G2Qprq9G6U2MyOaEm8TotivdpQh5fFt8tQVSPUK-_R_Mwv55Wpw0rd_Pn03IAKkE3jUufQ";
    public String getPeers(Long page, Long size, String campusId, Filter filter) {
        ArrayList<String> peers = new ArrayList<>();
        String newUrl = url + "/campuses/"+ campusId + "/participants?limit="+ size +"&offset="+ page;
        return createResponse(newUrl, accessToken);
    }

    private String createResponse(String url, String accessToken) {
        try {
            return webClient.build()
                    .get()
                    .uri(url)
                    .header("accept", "*/*")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch response from API: " + e.getMessage());
        }
    }
}