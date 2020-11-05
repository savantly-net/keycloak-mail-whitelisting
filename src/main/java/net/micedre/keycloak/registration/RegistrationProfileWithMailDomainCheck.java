package net.micedre.keycloak.registration;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.authentication.forms.RegistrationPage;
import org.keycloak.authentication.forms.RegistrationProfile;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.validation.Validation;

public class RegistrationProfileWithMailDomainCheck extends RegistrationProfile implements FormAction {

   public static final String PROVIDER_ID = "registration-mail-check-action";

   @Override
    public String getDisplayType() {
        return "Profile Validation with email domain check";
   }


   @Override
   public String getId() {
      return PROVIDER_ID;
   }

   @Override
    public boolean isConfigurable() {
        return true;
   }


   @Override
   public String getHelpText() {
      return "Adds validation of domain emails for registration";
   }

   private static final List<ProviderConfigProperty> CONFIG_PROPERTIES = new ArrayList<ProviderConfigProperty>();

   static {
      ProviderConfigProperty property;
      property = new ProviderConfigProperty();
      property.setName("validDomains");
      property.setLabel("Valid domain for emails");
      property.setType(ProviderConfigProperty.MULTIVALUED_STRING_TYPE);
      property.setHelpText("List mail domains authorized to register");
      CONFIG_PROPERTIES.add(property);
   }

   @Override
   public List<ProviderConfigProperty> getConfigProperties() {
      return CONFIG_PROPERTIES;
   }

   @Override
   public void validate(ValidationContext context) {
      MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();

      List<FormMessage> errors = new ArrayList<>();
      String email = formData.getFirst(Validation.FIELD_EMAIL);

      boolean emailDomainValid = false;
      AuthenticatorConfigModel mailDomainConfig = context.getAuthenticatorConfig();
      String eventError = Errors.INVALID_REGISTRATION;

      String[] domains = mailDomainConfig.getConfig().getOrDefault("validDomains","example.org").split("##");
      for (String domain : domains) {
         if (email.endsWith(domain)) {
            emailDomainValid = true;
            break;
         }
      }
      if (!emailDomainValid) {
         System.out.println("here");
         context.getEvent().detail(Details.EMAIL, email);
         errors.add(new FormMessage(RegistrationPage.FIELD_EMAIL, Messages.INVALID_EMAIL));
      }
      if (errors.size() > 0) {
         context.error(eventError);
         context.validationError(formData, errors);
         return;

      } else {
         context.success();
      }

   }

}