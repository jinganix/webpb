import io.github.jinganix.webpb.processor.model.FooRequest;
import io.github.jinganix.webpb.runtime.messaging.WebpbMessageMapping;

class SubMessageRequest extends FooRequest {
}

public class Sample9 {

    @WebpbMessageMapping
    void sample(SubMessageRequest request) {
    }
}
