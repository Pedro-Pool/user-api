package com.user.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

class Argon2BenchmarkTest {

    private static final String PASSWORD = "StrongPassword@123";
    private static final int WARMUP_ITERATIONS = 3;
    private static final int BENCHMARK_ITERATIONS = 10;

    @Test
    @DisplayName("Benchmark Argon2id configurations")
    void benchmarkConfigurations(){
        benchmark("Current", Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8());

        benchmark("Intermediate", new Argon2PasswordEncoder(
                16,32,1,32768,3
        ));
    }

    private void benchmark(String name, Argon2PasswordEncoder encoder){
        for (int i = 0; i < WARMUP_ITERATIONS; i++){
            encoder.encode(PASSWORD);
        }

        long start = System.nanoTime();

        for (int i = 0; i < BENCHMARK_ITERATIONS; i++){
            encoder.encode(PASSWORD);
        }

        long elapsed = System.nanoTime() - start;

        double averageMs = (double) elapsed
                / BENCHMARK_ITERATIONS
                /1_000_000;

        System.out.printf("%s: %.2f ms per hash%n", name, averageMs);
    }
}
