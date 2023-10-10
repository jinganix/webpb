import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.jinganix.webpb.processor.model.FooRequest;
import io.github.jinganix.webpb.runtime.messaging.WebpbMessageMapping;

public class Sample6 {

    @JsonIgnore
    @WebpbMessageMapping
    void sample(FooRequest request) {
    }
}
