package dk.dma.msinm.common.templates;

import freemarker.core.Environment;
import freemarker.template.SimpleNumber;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

/**
 * Free marker directive for controlling line flow
 */
public class LineDirective implements TemplateDirectiveModel {

    private static final String PARAM_MAX_LENGTH = "maxLength";
    private static final String PARAM_CASE = "case";

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(Environment env,
                        Map params, TemplateModel[] loopVars,
                        TemplateDirectiveBody body)
            throws TemplateException, IOException {

        SimpleNumber maxLengthModel = (SimpleNumber) params.get(PARAM_MAX_LENGTH);
        int maxLength = (maxLengthModel == null) ? -1 : maxLengthModel.getAsNumber().intValue();

        SimpleScalar caseModel = (SimpleScalar) params.get(PARAM_CASE);
        boolean lowerCase = (caseModel != null && "lower".equalsIgnoreCase(caseModel.getAsString()));
        boolean upperCase = (caseModel != null && "upper".equalsIgnoreCase(caseModel.getAsString()));

        // If there is non-empty nested content:
        if (body != null) {
            // Executes the nested body.
            StringWriter bodyWriter = new StringWriter();
            body.render(bodyWriter);

            // Process the result
            String s = bodyWriter.toString();
            s = s.replace("\n", " ");
            s = s.replace("\r", " ");
            s = s.replaceAll("\\s+", " ").trim();
            s = s.replace(" ,", ",");
            s = s.replace(" .", ".");

            if (upperCase) {
                s = s.toUpperCase();
            } else if (lowerCase) {
                s = s.toLowerCase();
            }

            if (maxLength != -1) {
                StringBuilder sb = new StringBuilder();
                while (s.length() > maxLength) {
                    String t = s.substring(0, maxLength);
                    int idx = Math.max(t.lastIndexOf(" "), t.lastIndexOf("\n"));
                    sb.append(s.substring(0, idx + 1)).append("\n");
                    s = s.substring(idx + 1);
                }
                sb.append(s);
                s = sb.toString();
            }

            // Write the result
            BufferedWriter out = new BufferedWriter(env.getOut());
            out.write(s);
            out.flush();
        } else {
            throw new RuntimeException("missing body");
        }
    }
}
