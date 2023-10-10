import io.github.jinganix.webpb.processor.model.FooRequest;
import io.github.jinganix.webpb.runtime.mvc.WebpbRequestMapping;

public class Sample2 {

    @WebpbRequestMapping(name = "sample2")
    void sample(FooRequest request) {
    }
}
