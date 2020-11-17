package io.spring.cloud.samples.animalrescue.backend.frontend

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    request {
        method 'GET'
        url '/animals'
        headers {
            contentType('application/json')
        }
    }
    response {
        status OK()
        body([
            [
              "id": $(regex("\\d+")),
              "name": anyNonBlankString(),
              "description": anyNonBlankString(),
              "avatarUrl": anyHttpsUrl(),
              "rescueDate": anyDate()
            ]
        ])
        headers {
            contentType('application/json')
        }
    }
}