import { ITypeScriptType } from "../typing";
import { ITypeSpec, IDefinitionSpec, IParameterSpec } from '../../common/typeSpec';

export class BooleanType implements ITypeScriptType {
  public doesHandleType(spec: ITypeSpec): boolean {
    return spec.type === 'boolean';
  }

  public getTypeScriptType(spec: ITypeSpec): string {
    return 'boolean';
  }

  public emitInterfaceDefinition(spec: IDefinitionSpec): string {
    return null;
  }
}