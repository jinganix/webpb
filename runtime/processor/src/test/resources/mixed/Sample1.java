import io.github.jinganix.webpb.processor.model.FooRequest;
import io.github.jinganix.webpb.runtime.messaging.WebpbMessageMapping;
import io.github.jinganix.webpb.runtime.mvc.WebpbRequestMapping;

public class Sample1 {

    @WebpbMessageMapping
    void sample1(FooRequest request) {
    }

    @WebpbRequestMapping
    void sample2(FooRequest request) {
    }
}

class Dummy {
}
