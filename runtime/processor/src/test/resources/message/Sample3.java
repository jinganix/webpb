import io.github.jinganix.webpb.processor.model.FooRequest;
import io.github.jinganix.webpb.runtime.messaging.WebpbMessageMapping;

public class Sample3 {

    @WebpbMessageMapping
    void sample(FooRequest request) {
    }
}
