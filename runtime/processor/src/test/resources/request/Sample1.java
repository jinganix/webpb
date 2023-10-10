import io.github.jinganix.webpb.processor.model.FooRequest;
import io.github.jinganix.webpb.runtime.mvc.WebpbRequestMapping;

public class Sample1 {

    @WebpbRequestMapping
    void sample(FooRequest request) {
    }
}

class Dummy {
}
