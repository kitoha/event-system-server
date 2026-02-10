package com.eventticketserver.consumer.config

import com.eventticketserver.common.event.TicketReservedEvent
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
import org.springframework.util.backoff.FixedBackOff

@Configuration
class KafkaConsumerConfig {

    @Value("\${spring.kafka.bootstrap-servers:localhost:9092}")
    private lateinit var bootstrapServers: String

    @Value("\${spring.kafka.properties.schema.registry.url:http://localhost:8081}")
    private lateinit var schemaRegistryUrl: String

    @Value("\${spring.kafka.listener.concurrency:3}")
    private var concurrency: Int = 3

    @Bean
    fun consumerFactory(): ConsumerFactory<String, TicketReservedEvent> {
        val props = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to ErrorHandlingDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to ErrorHandlingDeserializer::class.java,
            ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS to StringDeserializer::class.java.name,
            ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS to KafkaAvroDeserializer::class.java.name,
            KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG to schemaRegistryUrl,
            KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG to true,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest"
        )
        return DefaultKafkaConsumerFactory(props)
    }

    @Bean
    fun kafkaListenerContainerFactory(
        kafkaTemplate: KafkaTemplate<Any, Any>
    ): ConcurrentKafkaListenerContainerFactory<String, TicketReservedEvent> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, TicketReservedEvent>()
        factory.consumerFactory = consumerFactory()
        factory.setConcurrency(concurrency)
        
        val recoverer = DeadLetterPublishingRecoverer(kafkaTemplate) { record, _ ->
            TopicPartition("\${record.topic()}.DLT", record.partition())
        }
        val errorHandler = DefaultErrorHandler(recoverer, FixedBackOff(1000L, 3L))
        factory.setCommonErrorHandler(errorHandler)
        
        return factory
    }
}