/**
 * @jest-environment node
 */

import {getAnimals, setBackendBaseUrl} from "./httpClient";

const {runStubs} = require("spring-cloud-contract-stub-runner");
const SECONDS = 1000;

const port = 8088;
let stubRunnerInstance;
beforeAll(async () => {
    stubRunnerInstance = await runStubs(`io.spring.cloud.samples.animalrescue:contracts:+:${port}`);
    setBackendBaseUrl(`http://localhost:${port}`);
}, 60 * SECONDS);

afterAll(() => {
    stubRunnerInstance.kill(9);
});

describe('httpClient', () => {

    it('fetches animals', async () => {
        let animals = await getAnimals();
        expect(animals.length).toBeGreaterThanOrEqual(1);
        expect(animals[0].id).toBeDefined();
        expect(animals[0].name).toBeDefined();
        expect(animals[0].description).toBeDefined();
        expect(animals[0].rescueDate).toBeDefined();
        expect(animals[0].avatarUrl).toBeDefined();
    });

});