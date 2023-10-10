import io.github.jinganix.webpb.processor.model.FooRequest;
import io.github.jinganix.webpb.runtime.messaging.WebpbMessageMapping;

public class Sample4 {

    @WebpbMessageMapping(message = FooRequest.class)
    void sample(FooRequest request) {
    }
}
