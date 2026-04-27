import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { FormProvider, useForm } from 'react-hook-form';
import { describe, expect, it } from 'vitest';

import { FormField } from './FormField';
import { Input } from './Input';

function Harness({
  defaultValues,
  required,
  description,
  errorMessage,
}: {
  defaultValues?: Record<string, unknown>;
  required?: boolean;
  description?: string;
  errorMessage?: string;
}) {
  const form = useForm({ defaultValues: defaultValues ?? { name: '' } });
  // Inject an error post-mount when requested.
  if (errorMessage && !form.formState.errors.name) {
    form.setError('name', { message: errorMessage });
  }
  return (
    <FormProvider {...form}>
      <FormField name="name" label="Name" required={required} description={description}>
        {(field) => <Input type="text" {...field} value={String(field.value ?? '')} />}
      </FormField>
    </FormProvider>
  );
}

describe('FormField', () => {
  it('renders the label and wires htmlFor → input id', () => {
    render(<Harness />);
    const input = screen.getByLabelText('Name');
    expect(input).toBeInTheDocument();
    expect(input.id).toMatch(/-name$/);
  });

  it('marks required visually + with aria-required', () => {
    render(<Harness required />);
    const input = screen.getByLabelText(/Name/);
    expect(input).toHaveAttribute('aria-required', 'true');
  });

  it('wires aria-describedby to description when present', () => {
    render(<Harness description="Your full name" />);
    const input = screen.getByLabelText('Name');
    const describedBy = input.getAttribute('aria-describedby');
    expect(describedBy).toBeTruthy();
    expect(document.getElementById(describedBy!)).toHaveTextContent('Your full name');
  });

  it('renders error message + sets aria-invalid + extends aria-describedby with errorId', () => {
    render(<Harness errorMessage="Required" />);
    const input = screen.getByLabelText('Name');
    expect(input).toHaveAttribute('aria-invalid', 'true');
    const describedBy = input.getAttribute('aria-describedby');
    expect(describedBy).toBeTruthy();
    expect(screen.getByRole('alert')).toHaveTextContent('Required');
  });

  it('integrates with RHF — typing updates form state', async () => {
    const user = userEvent.setup();
    render(<Harness />);
    const input = screen.getByLabelText('Name');
    await user.type(input, 'Alice');
    expect(input).toHaveValue('Alice');
  });
});
