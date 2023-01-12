package nl.meine.scaler;

import nl.meine.scaler.up.WakeOnLan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Configurator {

    @Bean
    public WakeOnLan createWOL(){
        return new WakeOnLan();
    }
}
