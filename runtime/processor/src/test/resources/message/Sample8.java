import io.github.jinganix.webpb.processor.model.FooRequest;
import io.github.jinganix.webpb.runtime.messaging.WebpbMessageMapping;

public enum Sample8 {
    A;

    @WebpbMessageMapping
    void sample(FooRequest request) {
    }
}
