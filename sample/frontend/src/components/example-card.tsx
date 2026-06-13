import { ChevronDown } from "lucide-react";
import { useState } from "react";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { cn } from "@/lib/utils";
import { type ExampleDefinition } from "@/options/option-definitions";
import { type RequestLog, HttpService } from "@/services/http.service";

interface ExampleCardProps {
  example: ExampleDefinition;
  httpService: HttpService;
}

function formatJson(value: unknown): string {
  if (value === undefined) {
    return "";
  }
  return JSON.stringify(value, null, 2);
}

function CodePanel({
  label,
  value,
}: {
  label: string;
  value: string;
}): React.JSX.Element {
  return (
    <div className="min-w-0 space-y-2">
      <h4 className="text-sm font-medium text-slate-700">{label}</h4>
      <pre className="overflow-x-auto rounded-lg bg-slate-950 p-4 text-xs text-slate-50">
        {value}
      </pre>
    </div>
  );
}

export function ExampleCard({
  example,
  httpService,
}: ExampleCardProps): React.JSX.Element {
  const initialValues = Object.fromEntries(
    example.fields.map((field) => [field.key, field.defaultValue]),
  );
  const [expanded, setExpanded] = useState(false);
  const [values, setValues] = useState<Record<string, string>>(initialValues);
  const [log, setLog] = useState<RequestLog | null>(null);
  const [loading, setLoading] = useState(false);

  const sendRequest = async (): Promise<void> => {
    setLoading(true);
    const request = example.createRequest(values);
    try {
      const result = await httpService.request(request, example.responseType);
      setLog(result.log);
    } catch (error) {
      const requestError = error as Error & { log?: RequestLog };
      setLog(requestError.log ?? { error: String(error) });
    } finally {
      setLoading(false);
    }
  };

  return (
    <Card>
      <CardHeader className="p-0">
        <button
          aria-expanded={expanded}
          className="flex w-full items-start gap-3 p-6 text-left"
          onClick={() => setExpanded((current) => !current)}
          type="button"
        >
          <ChevronDown
            className={cn(
              "mt-0.5 h-5 w-5 shrink-0 text-slate-500 transition-transform",
              expanded && "rotate-180",
            )}
          />
          <div className="min-w-0 flex-1 space-y-1.5">
            <div className="flex items-center gap-2">
              <CardTitle>{example.title}</CardTitle>
              {example.category ? <Badge>{example.category}</Badge> : null}
            </div>
            <CardDescription>{example.description}</CardDescription>
          </div>
        </button>
      </CardHeader>
      {expanded ? (
        <CardContent className="space-y-4">
          {example.fields.map((field) => (
            <div className="space-y-2" key={field.key}>
              <Label htmlFor={`${example.id}-${field.key}`}>
                {field.label}
              </Label>
              <Input
                id={`${example.id}-${field.key}`}
                onChange={(event) =>
                  setValues((current) => ({
                    ...current,
                    [field.key]: event.target.value,
                  }))
                }
                value={values[field.key] ?? ""}
              />
            </div>
          ))}
          <Button disabled={loading} onClick={() => void sendRequest()}>
            {loading ? "Sending..." : "Send request"}
          </Button>
          <div className="grid gap-4 lg:grid-cols-3">
            <CodePanel label="Proto" value={example.protoSnippet} />
            <CodePanel
              label="Request"
              value={formatJson(log?.request ?? example.createRequest(values))}
            />
            <CodePanel
              label="Response"
              value={formatJson(
                log?.response ?? log?.error ?? "No response yet",
              )}
            />
          </div>
        </CardContent>
      ) : null}
    </Card>
  );
}
