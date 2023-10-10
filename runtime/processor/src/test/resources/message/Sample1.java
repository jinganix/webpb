import io.github.jinganix.webpb.processor.model.FooRequest;
import io.github.jinganix.webpb.runtime.messaging.WebpbMessageMapping;

public class Sample1 {

    @WebpbMessageMapping
    void sample(FooRequest request) {
    }
}

class Dummy {
}
