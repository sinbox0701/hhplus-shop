package kr.hhplus.be.server.config

import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.*

@Configuration
class KafkaConfig(
    @Value("\${spring.kafka.bootstrap-servers:localhost:29092}") private val bootstrapServers: String
) {
    companion object {
        const val COUPON_PUBLISH_REQUEST_TOPIC = "coupon-publish-request"
        const val COUPON_CONSUMER_GROUP_ID = "coupon-consumer-group"
    }

    // 토픽 생성
    @Bean
    fun couponPublishRequestTopic(): NewTopic {
        return TopicBuilder.name(COUPON_PUBLISH_REQUEST_TOPIC)
            .partitions(3) // 병렬 처리를 위해 3개의 파티션 설정
            .replicas(1)
            .build()
    }

    // Producer 설정
    @Bean
    fun producerFactory(): ProducerFactory<String, String> {
        val configProps = HashMap<String, Any>()
        configProps[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        configProps[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        configProps[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        return DefaultKafkaProducerFactory(configProps)
    }

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, String> {
        return KafkaTemplate(producerFactory())
    }

    // Consumer 설정
    @Bean
    fun consumerFactory(): ConsumerFactory<String, String> {
        val configProps = HashMap<String, Any>()
        configProps[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        configProps[ConsumerConfig.GROUP_ID_CONFIG] = COUPON_CONSUMER_GROUP_ID
        configProps[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        configProps[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        configProps[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        return DefaultKafkaConsumerFactory(configProps)
    }

    @Bean
    fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, String> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, String>()
        factory.consumerFactory = consumerFactory()
        factory.setConcurrency(3) // 컨슈머 인스턴스 수
        return factory
    }
} 