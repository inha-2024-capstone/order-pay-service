package com.project.yogerOrder.payment.config;

import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.response.AccessToken;
import com.siot.IamportRestClient.response.IamportResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
public class PortOneConfigTest {

    @Autowired
    private IamportClient iamportClient;

    @Test
    @DisplayName("PortOne client 정상 테스트")
    public void portOneConfig() throws IamportResponseException, IOException {
        IamportResponse<AccessToken> auth = iamportClient.getAuth();

        Assertions.assertThat(auth).isNotNull();
        Assertions.assertThat(auth.getCode()).isEqualTo(0);
    }
}