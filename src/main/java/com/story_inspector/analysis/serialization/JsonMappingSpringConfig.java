package com.story_inspector.analysis.serialization;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.story_inspector.analysis.Analyzer;
import com.story_inspector.analysis.AnalyzerCreationResult;
import com.story_inspector.analysis.AnalyzerSpec;
import com.story_inspector.analysis.AnalyzerType;
import com.story_inspector.analysis.AnalyzerTypeRegistry;

/**
 * Generates beans for JSON serialization and deserialization.
 *
 * @author mizitch
 *
 */
@Configuration
public class JsonMappingSpringConfig {

	@Autowired
	private AnalyzerTypeRegistry registry;

	/**
	 * Generates an {@link ObjectMapper} to serialize and deserialize JSON.
	 *
	 * @return The generated {@link ObjectMapper}.
	 */
	@Bean
	public ObjectMapper getDefaultObjectMapper() {
		final ObjectMapper mapper = new ObjectMapper();

		final SimpleModule module = new SimpleModule("AnalysisSerializers", new Version(1, 0, 0, null, null, null));

		// Add custom serializer & deserializer for AnalyzerType so that we can retrieve the correct singleton instance from spring
		module.addSerializer(AnalyzerType.class, new AnalyzerTypeSerializer());
		module.addDeserializer(AnalyzerType.class, new AnalyzerTypeDeserializer());

		// Add custom serializer & deserializer for Analyzer so that we can save the parameter values used to create the analyzer
		module.addSerializer(Analyzer.class, new AnalyzerSerializer());
		module.addDeserializer(Analyzer.class, new AnalyzerDeserializer());
		mapper.registerModule(module);

		// Save type information in JSON when declared type is object so it is deserialized correctly
		// Used because AnalyzerSpec has a map of string to object for saved parameter values
		mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT);

		return mapper;
	}

	/**
	 * Custom serializer for {@link AnalyzerType}, just writes the id and version.
	 *
	 * @author mizitch
	 *
	 */
	@SuppressWarnings("rawtypes")
	private class AnalyzerTypeSerializer extends JsonSerializer<AnalyzerType> {

		@Override
		public void serialize(final AnalyzerType value, final JsonGenerator gen, final SerializerProvider serializers)
				throws IOException, JsonProcessingException {
			gen.writeStartObject();
			gen.writeObjectField("id", value.getId());
			gen.writeObjectField("version", value.getVersion());
			gen.writeEndObject();
		}
	}

	/**
	 * Custom deserializer for {@link AnalyzerType}, reads the id and version and uses that to retrieve the matching registered {@link AnalyzerType}
	 * singleton.
	 *
	 * @author mizitch
	 *
	 */
	@SuppressWarnings("rawtypes")
	private class AnalyzerTypeDeserializer extends JsonDeserializer<AnalyzerType> {

		@Override
		public AnalyzerType<?> deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
			final JsonNode typeNode = p.getCodec().readTree(p);
			final String id = typeNode.get("id").asText();
			final int version = typeNode.get("version").asInt();
			return JsonMappingSpringConfig.this.registry.getAnalyzerTypeByIdAndVersion(id, version);
		}

	}

	/**
	 * Custom serializer for {@link Analyzer}. Does this by extracting the {@link AnalyzerSpec} and writing that.
	 *
	 * @author mizitch
	 *
	 */
	@SuppressWarnings("rawtypes")
	private class AnalyzerSerializer extends StdSerializer<Analyzer> {
		private static final long serialVersionUID = 1L;

		private AnalyzerSerializer() {
			super(Analyzer.class);
		}

		@Override
		public void serialize(final Analyzer value, final JsonGenerator gen, final SerializerProvider serializers)
				throws IOException, JsonProcessingException {
			final AnalyzerSpec spec = value.extractAnalyzerSpec();
			gen.writeObject(spec);
		}

		@Override
		public void serializeWithType(final Analyzer value, final JsonGenerator gen, final SerializerProvider serializers,
				final TypeSerializer typeSer) throws IOException {
			serialize(value, gen, serializers);
		}
	}

	/**
	 * Custom deserializer for {@link Analyzer}. Does this by reading an {@link AnalyzerSpec} and creating the {@link Analyzer} from that.
	 *
	 * @author mizitch
	 *
	 */
	@SuppressWarnings("rawtypes")
	private class AnalyzerDeserializer extends StdDeserializer<Analyzer> {
		private static final long serialVersionUID = 1L;

		private AnalyzerDeserializer() {
			super(Analyzer.class);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Analyzer<?> deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
			final JsonDeserializer<?> specDeserializer = ctxt
					.findNonContextualValueDeserializer(TypeFactory.defaultInstance().uncheckedSimpleType(AnalyzerSpec.class));

			final AnalyzerSpec spec = (AnalyzerSpec) specDeserializer.deserialize(p, ctxt);

			final AnalyzerCreationResult creationResult = spec.getAnalyzerType().tryCreateAnalyzer(spec);
			if (!creationResult.wasSuccessful()) {
				throw new IOException(
						"Could not create analyzer from spec, global result: " + creationResult.getGlobalResult() + ", parameter results: ");
			}

			return creationResult.getAnalyzer();
		}

		@Override
		public Object deserializeWithType(final JsonParser p, final DeserializationContext ctxt, final TypeDeserializer typeDeserializer)
				throws IOException {
			return this.deserialize(p, ctxt);
		}
	}

}
